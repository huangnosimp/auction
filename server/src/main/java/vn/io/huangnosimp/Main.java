package vn.io.huangnosimp;

import vn.io.huangnosimp.controller.MessageRouter;
import vn.io.huangnosimp.controller.handler.AuctionHandler;
import vn.io.huangnosimp.controller.handler.UserHandler;
import vn.io.huangnosimp.database.DatabaseConnection;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.model.SocketServer;
import vn.io.huangnosimp.repository.*;
import vn.io.huangnosimp.service.*;

public class Main {
    public static void main(String[] args) {
        //Database Connection
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        //Repositories
        IUserRepository userRepository = new UserRepository(dbConnection);
        IItemRepository itemRepository = new ItemRepository(dbConnection);
        IAuctionRepository auctionRepository = new AuctionRepository(dbConnection, userRepository, itemRepository);
        ITransactionRepository transactionRepository = new TransactionRepository    (dbConnection);
        //Services
        UserService userService = new UserService(userRepository);
        ItemService itemService = new ItemService(itemRepository);
        AuctionService auctionService = new AuctionService(auctionRepository, userService, itemService, transactionRepository);

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
        router.registerHandler(ActionType.DEPOSIT, new UserHandler.DepositHandler(userService));
        router.registerHandler(ActionType.WITHDRAW, new UserHandler.WithdrawHandler(userService));
        //Auction Handler
        router.registerHandler(ActionType.GET_AUCTION_DETAIL, new AuctionHandler.GetAuctionDetailHandler(auctionService));
        router.registerHandler(ActionType.CREATE_AUCTION, new AuctionHandler.CreateAuctionHandler(auctionService));
        router.registerHandler(ActionType.CANCEL_AUCTION, new AuctionHandler.CancelAuctionHandler(auctionService));
        router.registerHandler(ActionType.PLACE_BID, new AuctionHandler.PlaceBidHandler(auctionService));
        router.registerHandler(ActionType.JOIN_ROOM, new AuctionHandler.JoinRoomHandler(auctionService));
        router.registerHandler(ActionType.LEAVE_ROOM, new AuctionHandler.LeaveRoomHandler(auctionService));
        //Start Server
        SocketServer server = new SocketServer(26676, router);
        Runtime.getRuntime().addShutdownHook(new Thread(AuctionScheduler::shutdown));
        server.start();
    }
}
