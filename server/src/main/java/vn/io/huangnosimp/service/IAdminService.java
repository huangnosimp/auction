package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.MemberDTO;
import vn.io.huangnosimp.model.Admin;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.model.Auction;

import java.util.List;

public interface IAdminService {
    Admin getAdmin(String adminId);
    List<MemberDTO> getAllMembers();
    boolean lockMember(String memberId, int durationInMinutes);
    boolean unlockMember(String memberId);
    List<AuctionCardDTO> getAllAuctions();
    boolean forceCancelAuction(String auctionId);
    double getSystemTotalRevenue();
}