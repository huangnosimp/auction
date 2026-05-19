package vn.io.huangnosimp.Manager;
import vn.io.huangnosimp.controller.*;

public class ControllerManager {
    private static LoginController loginController;
    private static DashboardController dashboardController;
    private static dashboard_homeController dashboardHomeController;
    private static liveAuctionController liveAuctionController;
    private static OpenSlotController openSlotController;
    private static AccountViewController accountViewController;
    public static void clearAllController(){
        loginController = null;
        dashboardController = null;
        dashboardHomeController = null;
        liveAuctionController = null;
        openSlotController = null;
        accountViewController = null;
    }
    //login
    public static LoginController getLoginController(){
        return loginController;
    }
    public static void setLoginController(LoginController lg){
        loginController = lg;
    }

    //dashBoard
    public static  DashboardController getDashboardController(){
        return dashboardController;
    }
    public static void setDashboardController(DashboardController db){
        dashboardController = db;
    }

    //dashboardHome
    public static dashboard_homeController getDashboardHomeController() {   
        return dashboardHomeController; }
    public static void setDashboardHomeController(dashboard_homeController c) {
        dashboardHomeController = c; }

    //liveAuction
    public static void setLiveAuctionController(liveAuctionController c){
        liveAuctionController = c;
    }
    public liveAuctionController getLiveAuctionController(){
        return liveAuctionController;
    }

    //open slot
    public static OpenSlotController getOpenSlotController(){
        return openSlotController;
    }
    public static void setOpenSlotController(OpenSlotController o){
        openSlotController = o;
    }

    //accountView
    public static void setAccountViewController(AccountViewController a){
        accountViewController = a;
    }
    public static AccountViewController getAccountViewController(){
        return accountViewController;
    }



}
