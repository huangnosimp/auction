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
    private static List<AuctionCardDTO> joiningListCard = new ArrayList<>();
    private static List<AuctionCardDTO> myListCard = new ArrayList<>();
    private static String auctionId;
    private static String username;
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
    public static AuctionCardDTO findWithId(String Id){
        for(AuctionCardDTO card : listCard){
            if(card.getAuctionId().equals(Id)){
                return card;
            }
        }
        return null;
    }
    //joining card
    public static void addJoiningCard(List<AuctionCardDTO> joining){
        joiningListCard.addAll(joining);
    }
    public static void addonejoiningCard(AuctionCardDTO card){
        joiningListCard.add(card);
    }
    public static List<AuctionCardDTO> getJoiningListCard(){
        return joiningListCard;
    }
    //My card
    public static void addMyCard(List<AuctionCardDTO> my){
        myListCard.addAll(my);
    }
    public static void addoneMyCard(AuctionCardDTO mycard){
        myListCard.add(mycard);
    }
    public static List<AuctionCardDTO> getMyListCard(){
        return myListCard;
    }
    public static void removeCard(List<AuctionCardDTO> list, String Id) {
        list.removeIf(card -> card.getAuctionId().equals(Id));
    }
    //auction id
    public static void setAuctionId(String Id){
        auctionId = Id;
    }
    public static String getAuctionId(){
        return auctionId;
    }
    //username
    public static void setUsername(String name){
        username = name;
    }
    public static String getUsername(){
        return username;
    }
}
