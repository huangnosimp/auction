package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;
import vn.io.huangnosimp.dto.response.AuctionActionResult;
import vn.io.huangnosimp.dto.response.BidResult;
import vn.io.huangnosimp.enums.AuctionStatus;
import vn.io.huangnosimp.enums.ItemCondition;
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

    public AuctionCardDTO createAuction(String sellerId, String itemName, String description, ItemType itemType,
                                            ItemAttributesDTO attributes, double startPrice, long startTime,
                                            long endTime, ItemCondition condition, double minimumIncrement, double buyNowPrice) {
        if (!isValidOpenAuctionInput(sellerId, itemName, description, itemType, attributes, startPrice)
                || endTime <= startTime || startTime > System.currentTimeMillis()) {
            return null;
        }
        Member seller = userService.getMember(sellerId);
        if (seller == null) {
            return null;
        }
        Item item = itemService.createItem(sellerId, itemName, description, itemType, attributes, condition);
        Auction auction = new Auction(item, seller, startPrice, startTime, endTime, minimumIncrement, buyNowPrice);
        auctionRepository.save(auction);
        scheduler.scheduleAuction(auction);
        return new AuctionCardDTO(auction.getId(), item.getName(), auction.getCurrentPrice(), 0, auction.getEndTime());
    }

    private boolean isValidOpenAuctionInput(String sellerId, String name, String description, ItemType type,
            ItemAttributesDTO attributes, double startPrice) {
        return sellerId != null && !sellerId.isBlank() && name != null && !name.isBlank() && description != null
                && !description.isBlank() && type != null && attributes != null && startPrice > 0;
    }

    public BidResult placeBid(String bidderId, String auctionId, double amount, boolean triggerAutoBid) {
        BidResult bidSuccess = BidResult.ERROR;
        Object lock = getAuctionLock(auctionId);
        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);
            if (auction == null) {
                return BidResult.AUCTION_NOT_FOUND;
            }
            if (auction.getStatus() != AuctionStatus.RUNNING) {
                return BidResult.AUCTION_ENDED;
            }
            if (!auctionParticipantsRepository.isParticipant(auctionId, bidderId)) {
                return BidResult.ERROR;
            }
            Member bidder = userService.getMember(bidderId);
            if (bidder == null || bidder.getId().equals(auction.getSeller().getId())) {
                return BidResult.ERROR;
            }
            
            boolean isFirstBid = auction.getCurrentWinnerId() == null;
            double requiredMinBid = isFirstBid ? auction.getCurrentPrice() : (auction.getCurrentPrice() + auction.getMinimumIncrement());
            if (amount < requiredMinBid) {
                return BidResult.BID_TOO_LOW;
            }

            String previousWinnerId = auction.getCurrentWinnerId();
            double previousPrice = auction.getCurrentPrice();

            if (previousWinnerId != null && previousWinnerId.equals(bidderId)) {
                double delta = amount - previousPrice;
                if (!bidder.freezeMoney(delta)) {
                    return BidResult.INSUFFICIENT_FUNDS;
                }
                auction.setCurrentPrice(amount);
            } else {
                if (!bidder.freezeMoney(amount)) {
                    return BidResult.INSUFFICIENT_FUNDS;
                }
                if (previousWinnerId != null) {
                    Member previousWinner = userService.getMember(previousWinnerId);
                    if (previousWinner == null) {
                        bidder.unfreezeMoney(amount);
                        userService.updateBalance(bidder.getId(), bidder.getAccountBalance());
                        userService.updateFrozenBalance(bidder.getId(), bidder.getFrozenBalance());
                        return BidResult.ERROR;
                    }
                    if (!previousWinner.unfreezeMoney(previousPrice)) {
                        bidder.unfreezeMoney(amount);
                        userService.updateBalance(bidder.getId(), bidder.getAccountBalance());
                        userService.updateFrozenBalance(bidder.getId(), bidder.getFrozenBalance());
                        return BidResult.ERROR;
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
                if (previousWinnerId != null && !previousWinnerId.equals(bidderId)) {
                    notificationService.notifyOutbid(auctionId, previousWinnerId, auction.getCurrentPrice());
                }
            }

            if (auction.needExtension() && scheduler != null) {
                long newEndTime = System.currentTimeMillis() + 60 * 1000;
                scheduler.extendTime(auction, newEndTime);
            }
            bidSuccess = BidResult.SUCCESS;
        }

        if (bidSuccess == BidResult.SUCCESS && triggerAutoBid && autoBidService != null) {
            autoBidService.processAutoBids(auctionId);
        }

        return bidSuccess;
    }

    public AuctionActionResult cancelAuction(String auctionId) {
        AuctionActionResult success = AuctionActionResult.ERROR;
        Object lock = getAuctionLock(auctionId);
        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);
            if (auction == null) {
                return AuctionActionResult.AUCTION_NOT_FOUND;
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
                success = AuctionActionResult.SUCCESS;
            } else {
                success = AuctionActionResult.INVALID_STATE;
            }
        }
        if (success == AuctionActionResult.SUCCESS || success == AuctionActionResult.INVALID_STATE) {
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
    public BidResult buyNow(String userId, String auctionId) {
        Object lock = getAuctionLock(auctionId);
        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);
            if (auction == null) {
                return BidResult.AUCTION_NOT_FOUND;
            }
            if (auction.getStatus() != AuctionStatus.RUNNING && auction.getStatus() != AuctionStatus.OPEN) {
                return BidResult.AUCTION_ENDED;
            }
            if (auction.getBuyNowPrice() <= 0) {
                return BidResult.ERROR;
            }
            Member buyer = userService.getMember(userId);
            if (buyer == null || buyer.getId().equals(auction.getSeller().getId())) {
                return BidResult.ERROR;
            }
            
            double buyNowPrice = auction.getBuyNowPrice();
            String previousWinnerId = auction.getCurrentWinnerId();
            double previousPrice = auction.getCurrentPrice();

            if (previousWinnerId != null && previousWinnerId.equals(userId)) {
                double delta = buyNowPrice - previousPrice;
                if (delta > 0 && !buyer.freezeMoney(delta)) {
                    return BidResult.INSUFFICIENT_FUNDS;
                }
            } else {
                if (!buyer.freezeMoney(buyNowPrice)) {
                    return BidResult.INSUFFICIENT_FUNDS;
                }
            }

            boolean transferSuccess = itemService.transferOwnership(auction.getItem(), buyer.getId());
            if (!transferSuccess) {
                if (previousWinnerId != null && previousWinnerId.equals(userId)) {
                    buyer.unfreezeMoney(buyNowPrice - previousPrice);
                } else {
                    buyer.unfreezeMoney(buyNowPrice);
                }
                return BidResult.ERROR;
            }

            if (previousWinnerId != null && !previousWinnerId.equals(userId)) {
                Member previousWinner = userService.getMember(previousWinnerId);
                if (previousWinner != null) {
                    previousWinner.unfreezeMoney(previousPrice);
                    userService.updateBalance(previousWinner.getId(), previousWinner.getAccountBalance());
                    userService.updateFrozenBalance(previousWinner.getId(), previousWinner.getFrozenBalance());
                }
            }

            buyer.deductFrozenMoney(buyNowPrice);
            userService.updateBalance(buyer.getId(), buyer.getAccountBalance());
            userService.updateFrozenBalance(buyer.getId(), buyer.getFrozenBalance());

            auction.setCurrentWinnerId(userId);
            auction.setCurrentPrice(buyNowPrice);
            auction.setStatusPaid();
            auctionRepository.save(auction);

            Member seller = auction.getSeller();
            seller.receivePayment(buyNowPrice);
            userService.updateBalance(seller.getId(), seller.getAccountBalance());

            if (transactionRepository != null) {
                transactionRepository.saveTransaction(new Transaction(userId, TransactionType.WITHDRAW, buyNowPrice));
                transactionRepository.saveTransaction(new Transaction(seller.getId(), TransactionType.DEPOSIT, buyNowPrice));
            }

            if (notificationService != null) {
                notificationService.notifyAuctionEnded(auctionId, buyer.getUsername(), buyNowPrice);
            }

            if (scheduler != null) {
                scheduler.cancelTimers(auctionId);
            }
        }
        auctionLocks.remove(auctionId);
        return BidResult.SUCCESS;
    }

    @Override
    public AuctionActionResult joinAuction(String userId, String auctionId, ClientHandle client) {
        Object lock = getAuctionLock(auctionId);
        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);
            if (auction == null) {
                return AuctionActionResult.AUCTION_NOT_FOUND;
            }
            if (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != AuctionStatus.RUNNING) {
                return AuctionActionResult.INVALID_STATE;
            }
            Member bidder = userService.getMember(userId);
            if (bidder == null) {
                return AuctionActionResult.ERROR;
            }
            auctionParticipantsRepository.addParticipant(auctionId, userId);
            ClientSessionManager.getInstance().joinRoom(auctionId, client);
            return AuctionActionResult.SUCCESS;
        }
    }

    @Override
    public AuctionActionResult leaveAuction(String userId, String auctionId, ClientHandle client) {
        Object lock = getAuctionLock(auctionId);
        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);
            if (auction == null) {
                return AuctionActionResult.AUCTION_NOT_FOUND;
            }
            Member bidder = userService.getMember(userId);
            if (bidder == null) {
                return AuctionActionResult.ERROR;
            }
            auctionParticipantsRepository.removeParticipant(auctionId, userId);
            ClientSessionManager.getInstance().leaveRoom(auctionId, client);
            return AuctionActionResult.SUCCESS;
        }
    }
}
