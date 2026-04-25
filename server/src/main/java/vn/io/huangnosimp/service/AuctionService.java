package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.AuctionResponseDTO;
import vn.io.huangnosimp.enums.AuctionStatus;
import vn.io.huangnosimp.enums.ItemType;
import vn.io.huangnosimp.enums.TransactionType;
import vn.io.huangnosimp.model.*;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.network.ClientSessionManager;
import vn.io.huangnosimp.dto.shared.ItemAttributesDTO;
import vn.io.huangnosimp.repository.IAuctionParticipantsRepository;
import vn.io.huangnosimp.repository.IAuctionRepository;
import vn.io.huangnosimp.repository.IBidTransactionRepository;
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
    private final IAuctionParticipantsRepository auctionParticipantsRepository;
    private final IBidTransactionRepository bidTransactionRepository;

    private final ConcurrentHashMap<String, Object> auctionLocks = new ConcurrentHashMap<>();

    private Object getAuctionLock(String auctionId) {
        return auctionLocks.computeIfAbsent(auctionId, k -> new Object());
    }

    public AuctionService(IAuctionRepository auctionRepository, IUserService userService, IItemService itemService,
            ITransactionRepository transactionRepository, IAuctionParticipantsRepository auctionParticipantsRepository,
            IBidTransactionRepository bidTransactionRepository) {
        this.auctionRepository = auctionRepository;
        this.userService = userService;
        this.itemService = itemService;
        this.transactionRepository = transactionRepository;
        this.auctionParticipantsRepository = auctionParticipantsRepository;
        this.bidTransactionRepository = bidTransactionRepository;
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
        return auction.toDTO(0, null);
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
            if (auction == null || auction.getStatus() != AuctionStatus.RUNNING) {
                return false;
            }
            if (!auctionParticipantsRepository.isParticipant(auctionId, bidderId)) {
                return false;
            }
            Member bidder = userService.getMember(bidderId);
            if (bidder == null || bidder.getId().equals(auction.getSeller().getId())) {
                return false;
            }
            if (amount <= auction.getCurrentPrice()) {
                return false;
            }

            String previousWinnerId = auction.getCurrentWinnerId();
            double previousPrice = auction.getCurrentPrice();

            if (previousWinnerId != null && previousWinnerId.equals(bidderId)) {
                double delta = amount - previousPrice;
                if (!bidder.freezeMoney(delta)) {
                    return false;
                }
                auction.setCurrentPrice(amount);
            } else {
                if (!bidder.freezeMoney(amount)) {
                    return false;
                }
                if (previousWinnerId != null) {
                    Member previousWinner = userService.getMember(previousWinnerId);
                    if (previousWinner == null) {
                        bidder.unfreezeMoney(amount);
                        userService.updateBalance(bidder.getId(), bidder.getAccountBalance());
                        userService.updateFrozenBalance(bidder.getId(), bidder.getFrozenBalance());
                        return false;
                    }
                    if (!previousWinner.unfreezeMoney(previousPrice)) {
                        bidder.unfreezeMoney(amount);
                        userService.updateBalance(bidder.getId(), bidder.getAccountBalance());
                        userService.updateFrozenBalance(bidder.getId(), bidder.getFrozenBalance());
                        return false;
                    }
                    userService.updateBalance(previousWinner.getId(), previousWinner.getAccountBalance());
                    userService.updateFrozenBalance(previousWinner.getId(), previousWinner.getFrozenBalance());
                }
                auction.setCurrentWinnerId(bidderId);
                auction.setCurrentPrice(amount);
            }

            userService.updateBalance(bidder.getId(), bidder.getAccountBalance());
            userService.updateFrozenBalance(bidder.getId(), bidder.getFrozenBalance());

            auction.setUpdatedAt(System.currentTimeMillis());
            auctionRepository.save(auction);
            bidTransactionRepository.saveBidTransaction(new BidTransaction(bidderId, auctionId, amount));

            if (notificationService != null) {
                notificationService.notifyBidPlaced(auctionId, auction.getCurrentPrice(), bidderId);
            }

            if (auction.needExtension() && scheduler != null) {
                long newEndTime = System.currentTimeMillis() + 60 * 1000;
                scheduler.extendTime(auction, newEndTime);
            }
            return true;
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
                    Member currentWinner = userService.getMember(currentWinnerId);
                    if (currentWinner != null) {
                        currentWinner.unfreezeMoney(auction.getCurrentPrice());
                        userService.updateBalance(currentWinner.getId(), currentWinner.getAccountBalance());
                        userService.updateFrozenBalance(currentWinner.getId(), currentWinner.getFrozenBalance());
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

    public void processPayment(String auctionId) {
        boolean success = false;
        Object lock = getAuctionLock(auctionId);

        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);

            if (auction != null && auction.getStatus() == AuctionStatus.FINISHED) {
                String bidderId = auction.getCurrentWinnerId();
                if (bidderId != null) {
                    Member winner = userService.getMember(bidderId);
                    Member seller = auction.getSeller();

                    if (winner != null && seller != null) {
                        if (winner.deductFrozenMoney(auction.getCurrentPrice())) {
                            seller.receivePayment(auction.getCurrentPrice());

                            success = itemService.transferOwnership(auction.getItem(), winner.getId());

                            if (success) {
                                auction.setStatusPaid();
                                auctionRepository.save(auction);
                                userService.updateBalance(winner.getId(), winner.getAccountBalance());
                                userService.updateFrozenBalance(winner.getId(), winner.getFrozenBalance());
                                userService.updateBalance(seller.getId(), seller.getAccountBalance());

                                if (transactionRepository != null) {
                                    transactionRepository.saveTransaction(new Transaction(bidderId,
                                            TransactionType.WITHDRAW, auction.getCurrentPrice()));
                                    transactionRepository.saveTransaction(new Transaction(seller.getId(),
                                            TransactionType.DEPOSIT, auction.getCurrentPrice()));
                                }

                                if (notificationService != null) {
                                    notificationService.notifyAuctionEnded(auctionId, winner.getUsername(),
                                            auction.getCurrentPrice());
                                }
                            } else {
                                winner.unfreezeMoney(auction.getCurrentPrice());
                                seller.withdraw(auction.getCurrentPrice());
                            }
                        }
                    }
                }
            }
        }
        if (success) {
            auctionLocks.remove(auctionId);
        }
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
            auctionParticipantsRepository.addParticipant(auctionId, userId);
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
            auctionParticipantsRepository.removeParticipant(auctionId, userId);
            ClientSessionManager.getInstance().leaveRoom(auctionId, client);
            return true;
        }
    }
}
