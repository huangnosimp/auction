package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.AuctionActionResult;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.dto.response.MemberDTO;
import vn.io.huangnosimp.enums.AuctionStatus;
import vn.io.huangnosimp.model.Admin;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.User;
import vn.io.huangnosimp.repository.IUserRepository;
import vn.io.huangnosimp.repository.IAuctionRepository;
import vn.io.huangnosimp.util.ModelMapper;

import java.util.List;
import java.util.stream.Collectors;

public class AdminService implements IAdminService {
    private final IUserRepository userRepo;
    private final IAuctionRepository auctionRepo;
    private final IAuctionService auctionService;

    public AdminService(IUserRepository userRepo, IAuctionRepository auctionRepo, IAuctionService auctionService) {
        this.userRepo = userRepo;
        this.auctionRepo = auctionRepo;
        this.auctionService = auctionService;
    }

    @Override
    public Admin getAdmin(String adminId) {
        User user = userRepo.findById(adminId);
        if (user instanceof Admin) {
            return (Admin) user;
        }
        return null;
    }

    @Override
    public List<MemberDTO> getAllMembers() {
        return userRepo.findAll().stream()
                .filter(user -> user instanceof Member)
                .map(user -> ModelMapper.toMemberDTO((Member) user))
                .collect(Collectors.toList());
    }

    @Override
    public boolean lockMember(String memberId) {
        var user = userRepo.findById(memberId);
        if (user instanceof Member) {
            Member member = (Member) user;
            member.setBanned(true);
            return userRepo.updateStatus(memberId, true);
        }
        return false;
    }

    @Override
    public boolean unlockMember(String memberId) {
        var user = userRepo.findById(memberId);
        if (user instanceof Member) {
            Member member = (Member) user;
            member.setBanned(false);
            return userRepo.updateStatus(memberId, false);
        }
        return false;
    }

    @Override
    public List<AuctionCardDTO> getAllAuctions() {
        return ModelMapper.toAuctionCardDTOList(auctionRepo.findAll());
    }

    @Override
    public boolean forceCancelAuction(String auctionId) {
        AuctionActionResult result = auctionService.cancelAuction(auctionId);
        return switch (result) {
            case SUCCESS -> true;
            case AUCTION_NOT_FOUND, UNAUTHORIZED, INVALID_STATE, ERROR -> false;
        };
    }

    @Override
    public double getSystemTotalRevenue() {
        return auctionRepo.findAll().stream()
                .filter(a -> a.getStatus() == AuctionStatus.PAID)
                .mapToDouble(a -> a.getCurrentPrice() * 0.05)
                .sum();
    }
}