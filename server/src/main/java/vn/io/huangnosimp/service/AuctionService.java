package vn.io.huangnosimp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionActionResult;
import vn.io.huangnosimp.dto.response.BidResult;
import vn.io.huangnosimp.dto.response.TransactionResult;
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

    private boolean persistWallet(Member member) {
        return userService.updateBalance(member.getId(), member.getAccountBalance()) == TransactionResult.SUCCESS
                && userService.updateFrozenBalance(member.getId(), member.getFrozenBalance()) == TransactionResult.SUCCESS;
    }

    private boolean persistBalance(Member member) {
        return userService.updateBalance(member.getId(), member.getAccountBalance()) == TransactionResult.SUCCESS;
    }

    private boolean savePaymentTransactions(String buyerId, String sellerId, double amount) {
        if (transactionRepository == null) {
            return true;
        }
        return transactionRepository.saveTransaction(new Transaction(buyerId, TransactionType.WITHDRAW, amount))
                && transactionRepository.saveTransaction(new Transaction(sellerId, TransactionType.DEPOSIT, amount));
    }

    private void restoreWallet(Member member, double accountBalance, double frozenBalance) {
        member.setAccountBalance(accountBalance);
        member.setFrozenBalance(frozenBalance);
        persistWallet(member);
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
                || minimumIncrement <= 0
                || (buyNowPrice > 0 && buyNowPrice < startPrice)
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
        if (!auctionRepository.save(auction)) {
            logger.error("Auction creation failed while saving auction auctionId={} sellerId={}", auction.getId(), sellerId);
            return null;
        }
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
                if (auctionRepository.save(auction)) {
                    logger.info("Auction started auctionId={}", auctionId);
                } else {
                    logger.error("Auction start failed while saving auctionId={}", auctionId);
                }
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
                if (auctionRepository.save(auction)) {
                    logger.info("Auction marked finished auctionId={}", auctionId);
                } else {
                    logger.error("Auction finish failed while saving auctionId={}", auctionId);
                    return;
                }
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
            Member previousWinner = null;

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
                    previousWinner = userService.getMember(previousWinnerId);
                    if (previousWinner == null) {
                        bidder.unfreezeMoney(amount);
                        persistWallet(bidder);
                        logger.error("Bid failed because previous winner was not found auctionId={} previousWinnerId={}",
                                auctionId, previousWinnerId);
                        return BidResult.ERROR;
                    }
                    if (!previousWinner.unfreezeMoney(previousPrice)) {
                        bidder.unfreezeMoney(amount);
                        persistWallet(bidder);
                        logger.error("Bid failed because previous winner funds could not be unfrozen auctionId={} previousWinnerId={}",
                                auctionId, previousWinnerId);
                        return BidResult.ERROR;
                    }
                }
                auction.setCurrentWinnerId(bidderId);
                auction.setCurrentPrice(amount);
            }

            if (!persistWallet(bidder) || (previousWinner != null && !persistWallet(previousWinner))) {
                rollbackBidWallets(bidder, previousWinner, amount, previousPrice, previousWinnerId);
                logger.error("Bid failed while persisting wallet changes auctionId={} bidderId={}", auctionId, bidderId);
                return BidResult.ERROR;
            }

            auction.setUpdatedAt(System.currentTimeMillis());
            boolean auctionExtended = false;
            long extendedEndTime = auction.getEndTime();
            if (auction.needExtension() && scheduler != null) {
                long newEndTime = System.currentTimeMillis() + 60 * 1000;
                auctionExtended = scheduler.extendTime(auction, newEndTime);
                if (auctionExtended) {
                    extendedEndTime = auction.getEndTime();
                }
            }

            if (!auctionRepository.save(auction)) {
                rollbackBidWallets(bidder, previousWinner, amount, previousPrice, previousWinnerId);
                logger.error("Bid failed while saving auction auctionId={} bidderId={}", auctionId, bidderId);
                return BidResult.ERROR;
            }

            if (!bidTransactionRepository.saveBidTransaction(new BidTransaction(auctionId, bidderId, amount))) {
                auction.setCurrentWinnerId(previousWinnerId);
                auction.setCurrentPrice(previousPrice);
                auctionRepository.save(auction);
                rollbackBidWallets(bidder, previousWinner, amount, previousPrice, previousWinnerId);
                logger.error("Bid failed while saving bid transaction auctionId={} bidderId={}", auctionId, bidderId);
                return BidResult.ERROR;
            }

            if (notificationService != null) {
                notificationService.notifyBidPlaced(auctionId, amount, bidder.getUsername());
                if (previousWinnerId != null && !previousWinnerId.equals(bidderId)) {
                    notificationService.notifyOutbid(auctionId, previousWinnerId, auction.getCurrentPrice());
                }
                if (auctionExtended) {
                    notificationService.notifyAuctionExtended(auctionId, extendedEndTime);
                }
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

    private void rollbackBidWallets(Member bidder, Member previousWinner, double amount, double previousPrice,
            String previousWinnerId) {
        if (previousWinnerId != null && previousWinnerId.equals(bidder.getId())) {
            double delta = amount - previousPrice;
            if (delta > 0) {
                bidder.unfreezeMoney(delta);
            }
            persistWallet(bidder);
            return;
        }

        bidder.unfreezeMoney(amount);
        persistWallet(bidder);
        if (previousWinner != null) {
            previousWinner.freezeMoney(previousPrice);
            persistWallet(previousWinner);
        }
    }

    @Override
    public AuctionActionResult cancelAuction(String userId, String auctionId) {
        return cancelAuctionInternal(userId, auctionId, false);
    }

    @Override
    public AuctionActionResult forceCancelAuction(String auctionId) {
        return cancelAuctionInternal(null, auctionId, true);
    }

    private AuctionActionResult cancelAuctionInternal(String userId, String auctionId, boolean force) {
        AuctionActionResult success;
        Object lock = getAuctionLock(auctionId);
        synchronized (lock) {
            Auction auction = auctionRepository.findById(auctionId);
            if (auction == null) {
                logger.warn("Auction cancel rejected because auction was not found auctionId={}", auctionId);
                return AuctionActionResult.AUCTION_NOT_FOUND;
            }
            if (!force && (userId == null || !userId.equals(auction.getSeller().getId()))) {
                logger.warn("Auction cancel rejected because user is not seller auctionId={} userId={}", auctionId, userId);
                return AuctionActionResult.UNAUTHORIZED;
            }
            if (auction.getStatus() == AuctionStatus.OPEN || auction.getStatus() == AuctionStatus.RUNNING) {
                String currentWinnerId = auction.getCurrentWinnerId();
                Member currentWinner = null;
                if (currentWinnerId != null) {
                    currentWinner = userService.getMember(currentWinnerId);
                    if (currentWinner == null || !currentWinner.unfreezeMoney(auction.getCurrentPrice())
                            || !persistWallet(currentWinner)) {
                        logger.error("Auction cancel failed while unfreezing current winner auctionId={} currentWinnerId={}",
                                auctionId, currentWinnerId);
                        success = AuctionActionResult.ERROR;
                    } else {
                        auction.setStatusCanceled();
                        if (!auctionRepository.save(auction)) {
                            currentWinner.freezeMoney(auction.getCurrentPrice());
                            persistWallet(currentWinner);
                            logger.error("Auction cancel failed while saving auction auctionId={}", auctionId);
                            success = AuctionActionResult.ERROR;
                        } else {
                            success = AuctionActionResult.SUCCESS;
                        }
                    }
                } else {
                    auction.setStatusCanceled();
                    success = auctionRepository.save(auction) ? AuctionActionResult.SUCCESS : AuctionActionResult.ERROR;
                }

                if (success == AuctionActionResult.SUCCESS && notificationService != null) {
                    notificationService.notifyAuctionCanceled(auctionId);
                }

                if (success == AuctionActionResult.SUCCESS && scheduler != null) {
                    scheduler.cancelTimers(auctionId);
                }
                if (success == AuctionActionResult.SUCCESS) {
                    logger.info("Auction canceled auctionId={} force={}", auctionId, force);
                }
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
                        double amount = auction.getCurrentPrice();
                        if (!winner.deductFrozenMoney(amount)) {
                            logger.error("Auction payment failed because frozen money could not be deducted auctionId={} winnerId={}",
                                    auctionId, winner.getId());
                        } else {
                            seller.receivePayment(amount);
                            boolean transferSuccess = itemService.transferOwnership(auction.getItem(), winner.getId());

                            if (!transferSuccess) {
                                winner.deposit(amount);
                                seller.withdraw(amount);
                                logger.error("Auction payment failed because ownership transfer failed auctionId={}", auctionId);
                            } else {
                                auction.setStatusPaid();
                                success = auctionRepository.save(auction)
                                        && persistWallet(winner)
                                        && persistBalance(seller)
                                        && savePaymentTransactions(bidderId, seller.getId(), amount);

                                if (!success) {
                                    itemService.transferOwnership(auction.getItem(), seller.getId());
                                    auction.setStatus(AuctionStatus.FINISHED);
                                    auctionRepository.save(auction);
                                    winner.deposit(amount);
                                    seller.withdraw(amount);
                                    persistWallet(winner);
                                    persistBalance(seller);
                                    logger.error("Auction payment failed while persisting payment state auctionId={}", auctionId);
                                } else {
                                    if (notificationService != null) {
                                        notificationService.notifyAuctionEnded(auctionId, winner.getUsername(), amount);
                                    }
                                    logger.info("Auction payment processed auctionId={} winnerId={} sellerId={} amount={}",
                                            auctionId, winner.getId(), seller.getId(), amount);
                                }
                            }
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
            AuctionStatus originalStatus = auction.getStatus();
            if (buyNowPrice < previousPrice) {
                logger.warn("Buy now rejected because buy now price is below current price auctionId={} userId={} buyNowPrice={} currentPrice={}",
                        auctionId, userId, buyNowPrice, previousPrice);
                return BidResult.BID_TOO_LOW;
            }

            Member seller = auction.getSeller();
            Member previousWinner = null;
            if (previousWinnerId != null && !previousWinnerId.equals(userId)) {
                previousWinner = userService.getMember(previousWinnerId);
                if (previousWinner == null) {
                    logger.error("Buy now failed because previous winner was not found auctionId={} previousWinnerId={}",
                            auctionId, previousWinnerId);
                    return BidResult.ERROR;
                }
            }

            double buyerBalanceBefore = buyer.getAccountBalance();
            double buyerFrozenBefore = buyer.getFrozenBalance();
            double sellerBalanceBefore = seller.getAccountBalance();
            double sellerFrozenBefore = seller.getFrozenBalance();
            double previousWinnerBalanceBefore = previousWinner != null ? previousWinner.getAccountBalance() : 0;
            double previousWinnerFrozenBefore = previousWinner != null ? previousWinner.getFrozenBalance() : 0;
            String originalOwnerId = auction.getItem().getOwnerId();

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
                restoreWallet(buyer, buyerBalanceBefore, buyerFrozenBefore);
                logger.error("Buy now failed because ownership transfer failed auctionId={} userId={}", auctionId, userId);
                return BidResult.ERROR;
            }

            if (previousWinner != null && !previousWinner.unfreezeMoney(previousPrice)) {
                itemService.transferOwnership(auction.getItem(), originalOwnerId);
                restoreWallet(buyer, buyerBalanceBefore, buyerFrozenBefore);
                logger.error("Buy now failed because previous winner funds could not be unfrozen auctionId={} previousWinnerId={}",
                        auctionId, previousWinnerId);
                return BidResult.ERROR;
            }

            if (!buyer.deductFrozenMoney(buyNowPrice)) {
                itemService.transferOwnership(auction.getItem(), originalOwnerId);
                restoreWallet(buyer, buyerBalanceBefore, buyerFrozenBefore);
                if (previousWinner != null) {
                    restoreWallet(previousWinner, previousWinnerBalanceBefore, previousWinnerFrozenBefore);
                }
                logger.error("Buy now failed because buyer frozen money could not be deducted auctionId={} userId={}",
                        auctionId, userId);
                return BidResult.ERROR;
            }

            auction.setCurrentWinnerId(userId);
            auction.setCurrentPrice(buyNowPrice);
            auction.setStatus(AuctionStatus.PAID);
            seller.receivePayment(buyNowPrice);

            boolean persisted = auctionRepository.save(auction)
                    && persistWallet(buyer)
                    && (previousWinner == null || persistWallet(previousWinner))
                    && persistBalance(seller)
                    && savePaymentTransactions(userId, seller.getId(), buyNowPrice);

            if (!persisted) {
                itemService.transferOwnership(auction.getItem(), originalOwnerId);
                auction.setCurrentWinnerId(previousWinnerId);
                auction.setCurrentPrice(previousPrice);
                auction.setStatus(originalStatus);
                auctionRepository.save(auction);
                restoreWallet(buyer, buyerBalanceBefore, buyerFrozenBefore);
                restoreWallet(seller, sellerBalanceBefore, sellerFrozenBefore);
                if (previousWinner != null) {
                    restoreWallet(previousWinner, previousWinnerBalanceBefore, previousWinnerFrozenBefore);
                }
                logger.error("Buy now failed while persisting payment state auctionId={} userId={}", auctionId, userId);
                return BidResult.ERROR;
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
