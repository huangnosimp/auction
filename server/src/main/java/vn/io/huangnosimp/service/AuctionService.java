package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.AuctionResponseDTO;
import vn.io.huangnosimp.enums.AuctionStatus;
import vn.io.huangnosimp.enums.ItemType;
import vn.io.huangnosimp.model.*;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.network.ClientSessionManager;
import vn.io.huangnosimp.dto.shared.ItemAttributesDTO;
import vn.io.huangnosimp.repository.IAuctionRepository;
import vn.io.huangnosimp.repository.ITransactionRepository;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionService implements IAuctionService {
    private final IAuctionRepository auctionRepository;
    private final IUserService userService;
    private final IItemService itemService;
    private AuctionScheduler scheduler;
    private NotificationService notificationService;
    private IAutoBidService autoBidService;
    private final ITransactionRepository transactionRepository;

    private final ConcurrentHashMap<String, Object> auctionLocks = new ConcurrentHashMap<>();

    private Object getAuctionLock(String auctionId) {
        return auctionLocks.computeIfAbsent(auctionId, k -> new Object());
    }

    public AuctionService(IAuctionRepository auctionRepository, IUserService userService, IItemService itemService,
            ITransactionRepository transactionRepository) {
        this.auctionRepository = auctionRepository;
        this.userService = userService;
        this.itemService = itemService;
        this.transactionRepository = transactionRepository;
    }

    public void setScheduler(AuctionScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setAutoBidService(IAutoBidService autoBidService) {
        this.autoBidService = autoBidService;
    }

    @Override
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    public AuctionResponseDTO createAuction(String sellerId, String itemName, String description, ItemType itemType,
            ItemAttributesDTO attributes, double startPrice, long startTime, long endTime) {
        if (!isValidOpenAuctionInput(sellerId, itemName, description, itemType, attributes, startPrice)
                || endTime <= startTime || startTime > System.currentTimeMillis()) {
            return null;
        }
        Member seller = userService.getMember(sellerId);
        if (seller == null) {
            return null;
        }
        Item item = itemService.createItem(sellerId, itemName, description, itemType, attributes);
        Auction auction = new Auction(item, seller, startPrice, startTime, endTime);
        auctionRepository.save(auction);
        scheduler.scheduleAuction(auction);
        return auction.toDTO();
    }

    private boolean isValidOpenAuctionInput(String sellerId, String name, String description, ItemType type,
            ItemAttributesDTO attributes, double startPrice) {
        return sellerId != null && !sellerId.isBlank() && name != null && !name.isBlank() && description != null
                && !description.isBlank() && type != null && attributes != null && startPrice > 0;
    }

    public boolean placeBid(String bidderId, String auctionId, double amount, boolean triggerAutoBid) {
        Object lock = getAuctionLock(auctionId);
        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);
            if (auction == null) {
                return false;
            }
            Member member = userService.getMember(bidderId);
            if (member == null) {
                return false;
            }
            boolean isSuccess = auction.placeBid(member, amount);
            if (isSuccess) {
                auction.setUpdatedAt(System.currentTimeMillis());
                auctionRepository.save(auction);

                if (notificationService != null) {
                    notificationService.notifyBidPlaced(auctionId, auction.getCurrentPrice(), bidderId);
                }

                if (auction.needExtension() && scheduler != null) {
                    long newEndTime = System.currentTimeMillis() + 60 * 1000;
                    scheduler.extendTime(auction, newEndTime);
                }
            }
            return isSuccess;
        }
    }

    public boolean cancelAuction(String auctionId) {
        boolean success = false;
        Object lock = getAuctionLock(auctionId);
        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);
            if (auction == null) {
                return false;
            }
            if (auction.getStatus() == AuctionStatus.OPEN || auction.getStatus() == AuctionStatus.RUNNING) {
                String currentWinnerId = auction.getCurrentWinnerId();
                if (currentWinnerId != null) {
                    Member currentWinner = auction.getBidder(currentWinnerId);
                    if (currentWinner != null) {
                        currentWinner.unfreezeMoney(auction.getCurrentPrice());
                    }
                }
                auction.setStatusCanceled();
                auctionRepository.save(auction);

                if (notificationService != null) {
                    notificationService.notifyAuctionCanceled(auctionId);
                }

                if (scheduler != null) {
                    scheduler.cancelTimers(auctionId);
                }
                success = true;
            }
        }
        if (success) {
            auctionLocks.remove(auctionId);
        }
        return success;
    }

    public boolean processPayment(String auctionId) {
        boolean success = false;
        Object lock = getAuctionLock(auctionId);
        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);
            if (auction == null) {
                return false;
            }
            if (auction.getStatus() == AuctionStatus.FINISHED) {
                String bidderId = auction.getCurrentWinnerId();
                if (bidderId == null) {
                    return false;
                }
                Member winner = auction.getBidder(bidderId);
                if (winner != null) {
                    if (!winner.deductFrozenMoney(auction.getCurrentPrice())) {
                        return false;
                    }
                    BidTransaction bidTransaction = new BidTransaction(auctionId, bidderId, auction.getCurrentPrice(),
                            false);
                    auction.getSeller().receivePayment(auction.getCurrentPrice());
                    auction.setStatusPaid();
                    auctionRepository.save(auction);
                    if (transactionRepository != null) {
                        transactionRepository.saveTransaction(bidTransaction, auction.getSeller().getId());
                    }

                    if (notificationService != null) {
                        notificationService.notifyAuctionEnded(auctionId, bidderId, auction.getCurrentPrice());
                    }

                    success = itemService.transferOwnership(auction.getItem(), winner.getId());
                }
            }
        }
        if (success) {
            auctionLocks.remove(auctionId);
        }
        return success;
    }

    @Override
    public boolean joinAuction(String userId, String auctionId, ClientHandle client) {
        Object lock = getAuctionLock(auctionId);
        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);
            if (auction == null
                    || (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != AuctionStatus.RUNNING)) {
                return false;
            }
            Member bidder = userService.getMember(userId);
            if (bidder == null) {
                return false;
            }
            auction.addBidder(bidder);
            auctionRepository.save(auction);
            ClientSessionManager.getInstance().joinRoom(auctionId, client);
            return true;
        }
    }

    @Override
    public boolean leaveAuction(String userId, String auctionId, ClientHandle client) {
        Object lock = getAuctionLock(auctionId);
        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);
            if (auction == null) {
                return false;
            }
            Member bidder = userService.getMember(userId);
            if (bidder == null) {
                return false;
            }
            auction.removeBidder(bidder);
            auctionRepository.save(auction);
            ClientSessionManager.getInstance().leaveRoom(auctionId, client);
            return true;
        }
    }

    public AuctionResponseDTO getAuctionDetail(String auctionId) {
        Auction auction = auctionRepository.findById(auctionId);
        if (auction == null) {
            return null;
        }
        return auction.toDTO();
    }
}
