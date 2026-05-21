package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.AuctionActionResult;
import vn.io.huangnosimp.dto.response.AuctionAdminDTO;
import vn.io.huangnosimp.dto.response.MemberDTO;
import vn.io.huangnosimp.enums.AuctionStatus;
import vn.io.huangnosimp.model.Admin;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.model.User;
import vn.io.huangnosimp.network.ClientSessionManager;
import vn.io.huangnosimp.repository.IUserRepository;
import vn.io.huangnosimp.repository.IAuctionRepository;
import vn.io.huangnosimp.util.ModelMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AdminService implements IAdminService {
    private static final double COMMISSION_RATE = 0.05;

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
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return userRepo.findAll().stream()
                .filter(user -> user instanceof Member)
                .map(user -> {
                    Member member = (Member) user;

                    MemberDTO dto = ModelMapper.toMemberDTO(member);

                    boolean isCurrentlyBanned = member.isBanned();
                    if (member.getBanUntil() != null) {
                        if (member.getBanUntil().isAfter(now)) {
                            isCurrentlyBanned = true;
                        } else {
                            isCurrentlyBanned = false;
                        }
                    }

                    if (isCurrentlyBanned) {
                        if (member.getBanUntil() != null) {
                            dto.setStatus("Bị Ban (đến " + dto.getBanUntil() + ")");
                        } else {
                            dto.setStatus("Bị Ban vĩnh viễn");
                        }
                    } else if (ClientSessionManager.getInstance().isUserOnline(member.getId())) {
                        dto.setStatus("Online");
                    } else {
                        dto.setStatus("Offline");
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean lockMember(String memberId, int durationInMinutes) {

        var user = userRepo.findById(memberId);
        if (user instanceof Member) {
            LocalDateTime unbanTime = null;

            if (durationInMinutes > 0) {
                unbanTime = LocalDateTime.now().plusMinutes(durationInMinutes);
            }

            boolean isSuccess = userRepo.updateBanStatus(memberId, true, unbanTime);

            if (isSuccess) {
                ClientSessionManager.getInstance().banUser(memberId);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean unlockMember(String memberId) {
        var user = userRepo.findById(memberId);
        if (user instanceof Member) {
            return userRepo.updateBanStatus(memberId, false, null);
        }
        return false;
    }

    @Override
    public List<AuctionAdminDTO> getAllAuctions() {
        return ModelMapper.toAuctionAdminDTOList(auctionRepo.findAll());
    }

    @Override
    public boolean forceCancelAuction(String auctionId) {
        AuctionActionResult result = auctionService.cancelAuction(auctionId);
        boolean isSuccess = switch (result) {
            case SUCCESS -> true;
            case AUCTION_NOT_FOUND, UNAUTHORIZED, INVALID_STATE, ERROR -> false;
        };

        if (isSuccess) {
            ClientSessionManager.getInstance().destroyRoom(auctionId);
        }

        return isSuccess;
    }

    @Override
    public double getSystemTotalRevenue() {
        return auctionRepo.findAll().stream()
                .filter(a -> a.getStatus() == AuctionStatus.PAID)
                .mapToDouble(a -> a.getCurrentPrice() * COMMISSION_RATE)
                .sum();
    }
}