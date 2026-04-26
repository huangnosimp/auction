package vn.io.huangnosimp.service;

import vn.io.huangnosimp.model.Admin;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.model.Auction;

import java.util.List;

public interface IAdminService {
    Admin getAdmin(String adminId);
    List<Member> getAllMembers();
    boolean lockMember(String memberId);
    boolean unlockMember(String memberId);
    List<Auction> getAllAuctions();
    boolean forceCancelAuction(String auctionId);
    double getSystemTotalRevenue();
}