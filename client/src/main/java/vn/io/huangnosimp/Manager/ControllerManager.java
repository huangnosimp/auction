package vn.io.huangnosimp.Manager;
import vn.io.huangnosimp.controller.*;

public class ControllerManager {
    private static DashboardController dashboardController;
    private static dashboard_homeController dashboardHomeController;

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


}
