package vn.io.huangnosimp.Manager;

import vn.io.huangnosimp.controller.AccountViewController;
import vn.io.huangnosimp.dto.response.AutoBidResponseDTO;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.AuctionDetailResponseDTO;
import vn.io.huangnosimp.dto.response.DashboardResponseDTO;

import java.util.*;

public class UserSession {
    private static DashboardResponseDTO dashboardInfo;
    private static AuctionDetailResponseDTO auctionDetail;
    private static List<AuctionCardDTO> listCard = new ArrayList<>();
    private static List<AuctionCardDTO> joiningListCard = new ArrayList<>();
    private static List<AuctionCardDTO> myListCard = new ArrayList<>();
    private static List<AuctionCardDTO> searchListCard = new ArrayList<>();
    private static Set<String> activeAutoBidAuctionIds = new HashSet<>();
    private static List<AutoBidResponseDTO> activeAutoBidList = new ArrayList<>();
    private static Map<String, Double> latestBid = new HashMap<>();

    private static String auctionId;
    private static String username;
    private static String email;
    private static double balance;

    public static void clearAllSession(){
        dashboardInfo = null;
        auctionDetail = null;
        auctionId = null;
        username = null;
        if (listCard != null) {
            listCard.clear();
        }
        if (joiningListCard != null) {
            joiningListCard.clear();
        }
        if (myListCard != null) {
            myListCard.clear();
        }
        activeAutoBidAuctionIds.clear();
        activeAutoBidList.clear();
        latestBid.clear();
    }
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
        if (joining != null) {
            joiningListCard.addAll(joining);
            for (AuctionCardDTO card : joining) {
                // Chỉ set latestBid khi đang winning (tiền vẫn đang frozen)
                // Nếu bị outbid (yourBid < currentPrice) thì server đã unfreeze → latestBid = 0
                if (card.getYourBid() >= card.getCurrentPrice() && card.getYourBid() > 0) {
                    setLatestBid(card.getAuctionId(), card.getYourBid());
                } else {
                    setLatestBid(card.getAuctionId(), 0);
                }
            }
        }
    }
    public static void addonejoiningCard(AuctionCardDTO card){
        joiningListCard.add(card);
        if (card != null) {
            if (card.getYourBid() >= card.getCurrentPrice() && card.getYourBid() > 0) {
                setLatestBid(card.getAuctionId(), card.getYourBid());
            } else {
                setLatestBid(card.getAuctionId(), 0);
            }
        }
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
    //search
    public static void addSearchedCard(AuctionCardDTO card){
        searchListCard.add(card);
    }
    public static List<AuctionCardDTO> getSearchListCard(){
        return searchListCard;
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
    //email
    public static void setEmail(String em){
        email = em;
    }
    public static String getEmail(){
        return email;
    }
    //balance
    public static void setBalance(double amount){
        balance = amount;
    }
    public static void addBalance(double newBalance){
        balance += newBalance;
    }
    public static void minusBalance(double amount){
        balance -= amount;
    }
    public static double getBalance(){
        return balance;
    }

    // Active Auto Bids
    public static void setActiveAutoBids(List<AutoBidResponseDTO> autoBids) {
        activeAutoBidAuctionIds.clear();
        activeAutoBidList.clear();
        if (autoBids != null) {
            activeAutoBidList.addAll(autoBids);
            for (AutoBidResponseDTO ab : autoBids) {
                activeAutoBidAuctionIds.add(ab.getAuctionId());
            }
        }
    }

    public static List<AutoBidResponseDTO> getActiveAutoBidList() {
        return activeAutoBidList;
    }

    public static boolean hasAutoBid(String auctionId) {
        return activeAutoBidAuctionIds.contains(auctionId);
    }

    public static AutoBidResponseDTO getAutoBid(String auctionId) {
        for (AutoBidResponseDTO ab : activeAutoBidList) {
            if (ab.getAuctionId().equals(auctionId)) {
                return ab;
            }
        }
        return null;
    }

    public static void addAutoBid(AutoBidResponseDTO autoBid) {
        if (autoBid != null) {
            activeAutoBidList.add(autoBid);
            activeAutoBidAuctionIds.add(autoBid.getAuctionId());
        }
    }

    public static void removeAutoBid(String auctionId) {
        activeAutoBidAuctionIds.remove(auctionId);
        activeAutoBidList.removeIf(ab -> auctionId.equals(ab.getAuctionId()));
    }

    public static Set<String> getActiveAutoBidAuctionIds() {
        return activeAutoBidAuctionIds;
    }

    // Latest Bid methods
    public static void setLatestBid(String auctionId, double amount) {
        latestBid.put(auctionId, amount);
    }

    public static double getLatestBid(String auctionId) {
        return latestBid.getOrDefault(auctionId, 0.0);
    }

    public static void removeLatestBid(String auctionId) {
        latestBid.remove(auctionId);
    }
}
