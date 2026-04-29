package vn.io.huangnosimp.Manager;

import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;

public class UserSession {
    private static DashboardResponseDTO dashboardInfo;
    private static AuctionDetailResponseDTO auctionDetail;
    //dashboard
    public static void setDashboardInfo(DashboardResponseDTO info) {
        dashboardInfo = info;
    }
    public static DashboardResponseDTO getDashboardInfo() {
        return dashboardInfo;
    }
    //liveAuction
    public static void setAuctionDetail(AuctionDetailResponseDTO info){
        auctionDetail = info;
    }
    public static AuctionDetailResponseDTO getAuctionDetail(){
        return auctionDetail;
    }
}
