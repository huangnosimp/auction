package vn.io.huangnosimp.Manager;

import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;

import java.util.ArrayList;
import java.util.List;

public class UserSession {
    private static DashboardResponseDTO dashboardInfo;
    private static AuctionDetailResponseDTO auctionDetail;
    private static List<AuctionCardDTO> listCard = new ArrayList<>();
    private static String auctionId;
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
    //List open slots
    public static void addToList(List<AuctionCardDTO> list){
        listCard.clear();
        listCard.addAll(list);
    }
    public static List<AuctionCardDTO> getListCard(){
        return listCard;
    }
    //auction id
    public static void setAuctionId(String Id){
        auctionId = Id;
    }
    public static String getAuctionId(){
        return auctionId;
    }
}
