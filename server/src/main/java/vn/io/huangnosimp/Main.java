package vn.io.huangnosimp;

import vn.io.huangnosimp.controller.MessageRouter;
import vn.io.huangnosimp.controller.handler.*;
import vn.io.huangnosimp.connection.CloudinaryConnection;
import vn.io.huangnosimp.connection.DatabaseConnection;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.network.SocketServer;
import vn.io.huangnosimp.repository.*;
import vn.io.huangnosimp.service.*;
import vn.io.huangnosimp.util.ModelMapper;

import javax.swing.*;
import java.util.TimeZone;

public class Main {
    public static void main(String[] args) {
        //Set up Timezone
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        //Database Connection
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        CloudinaryConnection cloudinaryConnection = CloudinaryConnection.getInstance();
        //Repositories
        IUserRepository userRepository = new UserRepository(dbConnection);
        IItemRepository itemRepository = new ItemRepository(dbConnection);
        IAuctionRepository auctionRepository = new AuctionRepository(dbConnection, userRepository, itemRepository);
        ITransactionRepository transactionRepository = new TransactionRepository(dbConnection);
        IAuctionParticipantsRepository auctionParticipantsRepository = new AuctionParticipantsRepository(dbConnection);
        IBidTransactionRepository bidTransactionRepository = new BidTransactionRepository(dbConnection);
        IStatisticRepository statisticRepository = new StatisticRepository(dbConnection);
        IAutoBidRepository autoBidRepository = new AutoBidRepository(userRepository, auctionRepository, dbConnection);
        //Utils
        ModelMapper.setUserRepository(userRepository);
        //Services
        IUserService userService = new UserService(userRepository);
        ItemService itemService = new ItemService(itemRepository);
        ICloudinaryService cloudinaryService = new CloudinaryService(cloudinaryConnection);
        IStatisticService statisticService = new StatisticService(statisticRepository);
        AutoBidService autoBidService = new AutoBidService(autoBidRepository, userRepository, auctionRepository);
        AuctionService auctionService = new AuctionService(auctionRepository, userService, itemService, transactionRepository, auctionParticipantsRepository, bidTransactionRepository);
        autoBidService.setAuctionService(auctionService);
        auctionService.setAutoBidService(autoBidService);
        IAdminService adminService = new AdminService(userRepository, auctionRepository, auctionService);

        NotificationService notificationService = new NotificationService();
        auctionService.setNotificationService(notificationService);

        AuctionScheduler auctionScheduler = new AuctionScheduler(auctionService);
        auctionService.setScheduler(auctionScheduler);
        //Controllers
        MessageRouter router = new MessageRouter();
        //User Handler
        router.registerHandler(ActionType.LOGIN, new UserHandler.LoginHandler(userService));
        router.registerHandler(ActionType.REGISTER, new UserHandler.RegisterHandler(userService));
        router.registerHandler(ActionType.LOGOUT, new UserHandler.LogoutHandler());
        router.registerHandler(ActionType.GET_SERVER_TIME, new TimeHandler.GetServerTimeHandler());
        router.registerHandler(ActionType.DEPOSIT, new UserHandler.DepositHandler(userService));
        router.registerHandler(ActionType.WITHDRAW, new UserHandler.WithdrawHandler(userService));
        //Auction Handler
        router.registerHandler(ActionType.CREATE_AUCTION, new AuctionHandler.CreateAuctionHandler(auctionService));
        router.registerHandler(ActionType.CANCEL_AUCTION, new AuctionHandler.CancelAuctionHandler(auctionService));
        router.registerHandler(ActionType.PLACE_BID, new AuctionHandler.PlaceBidHandler(auctionService));
        router.registerHandler(ActionType.JOIN_ROOM, new AuctionHandler.JoinRoomHandler(auctionService));
        router.registerHandler(ActionType.LEAVE_ROOM, new AuctionHandler.LeaveRoomHandler(auctionService));
        router.registerHandler(ActionType.BUY_NOW, new AuctionHandler.BuyNowHandler(auctionService));
        //Cloudinary Handler
        router.registerHandler(ActionType.GET_CLOUDINARY_UPLOAD_SIGNATURE, new CloudinaryHandler.GetUploadSignatureHandler(cloudinaryService));
        //Admin Handler
        router.registerHandler(ActionType.ADMIN_GET_ALL_MEMBERS, new AdminHandler.GetAllMembersHandler(adminService));
        router.registerHandler(ActionType.ADMIN_LOCK_MEMBER, new AdminHandler.LockMemberHandler(adminService));
        router.registerHandler(ActionType.ADMIN_UNLOCK_MEMBER, new AdminHandler.UnlockMemberHandler(adminService));
        router.registerHandler(ActionType.ADMIN_GET_ALL_AUCTIONS, new AdminHandler.GetAllAuctionsHandler(adminService));
        router.registerHandler(ActionType.ADMIN_FORCE_CANCEL, new AdminHandler.ForceCancelAuctionHandler(adminService));
        router.registerHandler(ActionType.ADMIN_GET_REVENUE, new AdminHandler.GetRevenueHandler(adminService));
        //AutoBid Handler
        router.registerHandler(ActionType.REGISTER_AUTO_BID, new AutoBidHandler.RegisterHandler(autoBidService));
        router.registerHandler(ActionType.UNREGISTER_AUTO_BID, new AutoBidHandler.UnregisterHandler(autoBidService));
        router.registerHandler(ActionType.GET_USER_AUTO_BIDS, new AutoBidHandler.GetUserAutoBidsHandler(autoBidService));
        //Statistic Handler
        router.registerHandler(ActionType.GET_DASHBOARD_INFO, new StatisticHandler.GetDashboardInfoHandler(statisticService));
        router.registerHandler(ActionType.GET_AUCTION_DETAIL, new StatisticHandler.GetAuctionDetailHandler(statisticService));
        router.registerHandler(ActionType.GET_JOINING_AUCTION_CARD, new StatisticHandler.GetJoiningAuctionCardHandler(statisticService));
        router.registerHandler(ActionType.GET_PUBLIC_AUCTION_CARD, new StatisticHandler.GetPublicAuctionCardHandler(statisticService));
        router.registerHandler(ActionType.GET_POSTED_AUCTION_CARD, new StatisticHandler.GetPostedAuctionCardHandler(statisticService));
        router.registerHandler(ActionType.GET_WON_AUCTION, new StatisticHandler.GetWonAuctionHandler(statisticService));
        router.registerHandler(ActionType.GET_ENDED_POSTED_AUCTION, new StatisticHandler.GetEndedPostedAuctionHandler(statisticService));
        //Start Server
        SocketServer server = new SocketServer(26676, router);
        Runtime.getRuntime().addShutdownHook(new Thread(AuctionScheduler::shutdown));
        server.start();
    }
}
