package vn.io.huangnosimp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionService implements IAuctionService {
    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);
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
        logger.info("Auction scheduler attached");
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
        logger.info("Notification service attached to auction service");
    }

    public void setAutoBidService(IAutoBidService autoBidService) {
        this.autoBidService = autoBidService;
        logger.info("Auto bid service attached to auction service");
    }

    @Override
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
            logger.info("Auction service shutdown completed");
        } else {
            logger.info("Auction service shutdown completed without scheduler");
        }
    }

    @Override
    public AuctionCardDTO createAuction(String sellerId, String itemName, String description, ItemType itemType,
                                            ItemAttributesDTO attributes, double startPrice, long startTime,
                                            long endTime, ItemCondition condition, double minimumIncrement,
                                            double buyNowPrice, List<String> imageUrl) {
        if (!isValidOpenAuctionInput(sellerId, itemName, description, itemType, attributes, startPrice)
                || endTime <= startTime || startTime < System.currentTimeMillis()) {
            logger.warn(
                    "Auction creation rejected sellerId={} itemType={} startPrice={} startTime={} endTime={}",
                    sellerId, itemType, startPrice, startTime, endTime);
            return null;
        }
        Member seller = userService.getMember(sellerId);
        if (seller == null) {
            logger.warn("Auction creation rejected because seller was not found sellerId={}", sellerId);
            return null;
        }
        Item item = itemService.createItem(sellerId, itemName, description, itemType, attributes, condition, imageUrl);
        Auction auction = new Auction(item, seller, startPrice, startTime, endTime, minimumIncrement, buyNowPrice);
        auctionRepository.save(auction);
        if (scheduler != null) {
            scheduler.scheduleAuction(auction);
        } else {
            logger.warn("Auction created without scheduler auctionId={}", auction.getId());
        }
        logger.info("Auction created auctionId={} sellerId={} itemId={} startPrice={}",
                auction.getId(), sellerId, item.getId(), startPrice);
        return new AuctionCardDTO(auction.getId(), item.getName(), auction.getStartPrice(), 0, auction.getStartTime(), auction.getEndTime(), 0, 0, imageUrl);
    }

    private boolean isValidOpenAuctionInput(String sellerId, String name, String description, ItemType type,
            ItemAttributesDTO attributes, double startPrice) {
        return sellerId != null && !sellerId.isBlank() && name != null && !name.isBlank() && description != null
                && !description.isBlank() && type != null && attributes != null && startPrice > 0;
    }

    public void startAuction(String auctionId) {
        Object lock = getAuctionLock(auctionId);
        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);
            if (auction != null && auction.getStatus() == AuctionStatus.OPEN) {
                auction.setStatusRunning();
                auctionRepository.save(auction);
                logger.info("Auction started auctionId={}", auctionId);
            } else {
                logger.warn("Auction start skipped auctionId={} found={} status={}",
                        auctionId, auction != null, auction != null ? auction.getStatus() : null);
            }
        }
    }

    public void finishAuction(String auctionId) {
        Object lock = getAuctionLock(auctionId);
        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);
            if (auction != null && auction.getStatus() == AuctionStatus.RUNNING) {
                auction.setStatusFinish();
                auctionRepository.save(auction);
                logger.info("Auction marked finished auctionId={}", auctionId);
            } else {
                logger.warn("Auction finish skipped auctionId={} found={} status={}",
                        auctionId, auction != null, auction != null ? auction.getStatus() : null);
            }
        }
        processPayment(auctionId);
    }

    @Override
    public BidResult placeBid(String bidderId, String auctionId, double amount, boolean triggerAutoBid) {
        BidResult bidSuccess;
        Object lock = getAuctionLock(auctionId);
        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);
            if (auction == null) {
                logger.warn("Bid rejected because auction was not found auctionId={} bidderId={}", auctionId, bidderId);
                return BidResult.AUCTION_NOT_FOUND;
            }
            if (auction.getStatus() != AuctionStatus.RUNNING) {
                logger.info("Bid rejected because auction is not running auctionId={} bidderId={} status={}",
                        auctionId, bidderId, auction.getStatus());
                return BidResult.AUCTION_ENDED;
            }
            if (!auctionParticipantsRepository.isParticipant(auctionId, bidderId)) {
                logger.info("Bid rejected because bidder is not in room auctionId={} bidderId={}", auctionId, bidderId);
                return BidResult.NOT_IN_ROOM;
            }
            Member bidder = userService.getMember(bidderId);
            if (bidder == null || bidder.getId().equals(auction.getSeller().getId())) {
                logger.warn("Bid rejected because bidder is invalid auctionId={} bidderId={}", auctionId, bidderId);
                return BidResult.ERROR;
            }
            
            boolean isFirstBid = auction.getCurrentWinnerId() == null;
            double requiredMinBid = isFirstBid ? auction.getCurrentPrice() : (auction.getCurrentPrice() + auction.getMinimumIncrement());
            if (amount < requiredMinBid) {
                logger.info("Bid rejected because amount is too low auctionId={} bidderId={} amount={} requiredMinBid={}",
                        auctionId, bidderId, amount, requiredMinBid);
                return BidResult.BID_TOO_LOW;
            }

            String previousWinnerId = auction.getCurrentWinnerId();
            double previousPrice = auction.getCurrentPrice();

            if (previousWinnerId != null && previousWinnerId.equals(bidderId)) {
                double delta = amount - previousPrice;
                if (!bidder.freezeMoney(delta)) {
                    logger.info("Bid rejected due to insufficient funds for delta auctionId={} bidderId={} delta={}",
                            auctionId, bidderId, delta);
                    return BidResult.INSUFFICIENT_FUNDS;
                }
                auction.setCurrentPrice(amount);
            } else {
                if (!bidder.freezeMoney(amount)) {
                    logger.info("Bid rejected due to insufficient funds auctionId={} bidderId={} amount={}",
                            auctionId, bidderId, amount);
                    return BidResult.INSUFFICIENT_FUNDS;
                }
                if (previousWinnerId != null) {
                    Member previousWinner = userService.getMember(previousWinnerId);
                    if (previousWinner == null) {
                        bidder.unfreezeMoney(amount);
                        userService.updateBalance(bidder.getId(), bidder.getAccountBalance());
                        userService.updateFrozenBalance(bidder.getId(), bidder.getFrozenBalance());
                        logger.error("Bid failed because previous winner was not found auctionId={} previousWinnerId={}",
                                auctionId, previousWinnerId);
                        return BidResult.ERROR;
                    }
                    if (!previousWinner.unfreezeMoney(previousPrice)) {
                        bidder.unfreezeMoney(amount);
                        userService.updateBalance(bidder.getId(), bidder.getAccountBalance());
                        userService.updateFrozenBalance(bidder.getId(), bidder.getFrozenBalance());
                        logger.error("Bid failed because previous winner funds could not be unfrozen auctionId={} previousWinnerId={}",
                                auctionId, previousWinnerId);
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
            bidTransactionRepository.saveBidTransaction(new BidTransaction(auctionId, bidderId, amount));

            if (notificationService != null) {
                notificationService.notifyBidPlaced(auctionId, amount, bidder.getUsername());
                if (previousWinnerId != null && !previousWinnerId.equals(bidderId)) {
                    notificationService.notifyOutbid(auctionId, previousWinnerId, auction.getCurrentPrice());
                }
            }

            if (auction.needExtension() && scheduler != null) {
                long newEndTime = System.currentTimeMillis() + 60 * 1000;
                scheduler.extendTime(auction, newEndTime);
            }
            logger.info("Bid placed auctionId={} bidderId={} amount={} previousWinnerId={}",
                    auctionId, bidderId, amount, previousWinnerId);
            bidSuccess = BidResult.SUCCESS;
        }

        if (bidSuccess == BidResult.SUCCESS && triggerAutoBid && autoBidService != null) {
            logger.debug("Triggering auto bids auctionId={}", auctionId);
            autoBidService.processAutoBids(auctionId);
        }

        return bidSuccess;
    }

    public AuctionActionResult cancelAuction(String auctionId) {
        AuctionActionResult success;
        Object lock = getAuctionLock(auctionId);
        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);
            if (auction == null) {
                logger.warn("Auction cancel rejected because auction was not found auctionId={}", auctionId);
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
                logger.info("Auction canceled auctionId={}", auctionId);
                success = AuctionActionResult.SUCCESS;
            } else {
                logger.info("Auction cancel rejected due to invalid state auctionId={} status={}", auctionId, auction.getStatus());
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
                                logger.info("Auction payment processed auctionId={} winnerId={} sellerId={} amount={}",
                                        auctionId, winner.getId(), seller.getId(), auction.getCurrentPrice());
                            } else {
                                winner.unfreezeMoney(auction.getCurrentPrice());
                                seller.withdraw(auction.getCurrentPrice());
                                logger.error("Auction payment failed because ownership transfer failed auctionId={}", auctionId);
                            }
                        } else {
                            logger.error("Auction payment failed because frozen money could not be deducted auctionId={} winnerId={}",
                                    auctionId, winner.getId());
                        }
                    } else {
                        logger.error("Auction payment skipped because winner or seller was missing auctionId={} winnerFound={} sellerFound={}",
                                auctionId, winner != null, seller != null);
                    }
                } else {
                    logger.info("Auction payment skipped because auction has no winner auctionId={}", auctionId);
                }
            } else {
                logger.warn("Auction payment skipped auctionId={} found={} status={}",
                        auctionId, auction != null, auction != null ? auction.getStatus() : null);
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
                logger.warn("Buy now rejected because auction was not found auctionId={} userId={}", auctionId, userId);
                return BidResult.AUCTION_NOT_FOUND;
            }
            if (auction.getStatus() != AuctionStatus.RUNNING && auction.getStatus() != AuctionStatus.OPEN) {
                logger.info("Buy now rejected because auction state is invalid auctionId={} userId={} status={}",
                        auctionId, userId, auction.getStatus());
                return BidResult.AUCTION_ENDED;
            }
            if (auction.getBuyNowPrice() <= 0) {
                logger.warn("Buy now rejected because price is not configured auctionId={} userId={}", auctionId, userId);
                return BidResult.ERROR;
            }
            Member buyer = userService.getMember(userId);
            if (buyer == null || buyer.getId().equals(auction.getSeller().getId())) {
                logger.warn("Buy now rejected because buyer is invalid auctionId={} userId={}", auctionId, userId);
                return BidResult.ERROR;
            }
            
            double buyNowPrice = auction.getBuyNowPrice();
            String previousWinnerId = auction.getCurrentWinnerId();
            double previousPrice = auction.getCurrentPrice();

            if (previousWinnerId != null && previousWinnerId.equals(userId)) {
                double delta = buyNowPrice - previousPrice;
                if (delta > 0 && !buyer.freezeMoney(delta)) {
                    logger.info("Buy now rejected due to insufficient funds for delta auctionId={} userId={} delta={}",
                            auctionId, userId, delta);
                    return BidResult.INSUFFICIENT_FUNDS;
                }
            } else {
                if (!buyer.freezeMoney(buyNowPrice)) {
                    logger.info("Buy now rejected due to insufficient funds auctionId={} userId={} amount={}",
                            auctionId, userId, buyNowPrice);
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
                logger.error("Buy now failed because ownership transfer failed auctionId={} userId={}", auctionId, userId);
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
            logger.info("Buy now completed auctionId={} userId={} amount={}", auctionId, userId, buyNowPrice);
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
                logger.warn("Join auction rejected because auction was not found auctionId={} userId={}", auctionId, userId);
                return AuctionActionResult.AUCTION_NOT_FOUND;
            }
            if (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != AuctionStatus.RUNNING) {
                logger.info("Join auction rejected due to invalid state auctionId={} userId={} status={}",
                        auctionId, userId, auction.getStatus());
                return AuctionActionResult.INVALID_STATE;
            }
            Member bidder = userService.getMember(userId);
            if (bidder == null) {
                logger.warn("Join auction rejected because user was not a member auctionId={} userId={}", auctionId, userId);
                return AuctionActionResult.ERROR;
            }
            auctionParticipantsRepository.addParticipant(auctionId, userId);
            ClientSessionManager.getInstance().joinRoom(auctionId, client);
            logger.info("User joined auction room auctionId={} userId={}", auctionId, userId);
            return AuctionActionResult.SUCCESS;
        }
    }

    @Override
    public AuctionActionResult leaveAuction(String userId, String auctionId, ClientHandle client) {
        Object lock = getAuctionLock(auctionId);
        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);
            if (auction == null) {
                logger.warn("Leave auction rejected because auction was not found auctionId={} userId={}", auctionId, userId);
                return AuctionActionResult.AUCTION_NOT_FOUND;
            }
            Member bidder = userService.getMember(userId);
            if (bidder == null) {
                logger.warn("Leave auction rejected because user was not a member auctionId={} userId={}", auctionId, userId);
                return AuctionActionResult.ERROR;
            }
            auctionParticipantsRepository.removeParticipant(auctionId, userId);
            ClientSessionManager.getInstance().leaveRoom(auctionId, client);
            logger.info("User left auction room auctionId={} userId={}", auctionId, userId);
            return AuctionActionResult.SUCCESS;
        }
    }
}
