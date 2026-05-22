package vn.io.huangnosimp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
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
        logger.debug("Admin lookup returned no admin userId={}", adminId);
        return null;
    }


    @Override
    public List<MemberDTO> getAllMembers() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        List<MemberDTO> members = userRepo.findAll().stream()
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
        logger.info("Loaded members for admin count={}", members.size());
        return members;
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
                logger.info("Member locked memberId={} durationInMinutes={}", memberId, durationInMinutes);
                return true;
            }
            logger.error("Member lock failed while updating ban status memberId={}", memberId);
        }

        logger.warn("Member lock rejected memberId={} durationInMinutes={}", memberId, durationInMinutes);
        return false;
    }

    @Override
    public boolean unlockMember(String memberId) {
        var user = userRepo.findById(memberId);
        if (user instanceof Member) {
            boolean unlocked = userRepo.updateBanStatus(memberId, false, null);
            if (unlocked) {
                logger.info("Member unlocked memberId={}", memberId);
            } else {
                logger.error("Member unlock failed while updating ban status memberId={}", memberId);
            }
            return unlocked;
        }
        logger.warn("Member unlock rejected because user was not a member memberId={}", memberId);
        return false;
    }

    @Override
    public List<AuctionAdminDTO> getAllAuctions() {
        List<AuctionAdminDTO> auctions = ModelMapper.toAuctionAdminDTOList(auctionRepo.findAll());
        logger.info("Loaded auctions for admin count={}", auctions.size());
        return auctions;
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
            logger.info("Auction force canceled by admin auctionId={}", auctionId);
        } else {
            logger.warn("Auction force cancel failed auctionId={} result={}", auctionId, result);
        }

        return isSuccess;
    }

    @Override
    public double getSystemTotalRevenue() {
        double revenue = auctionRepo.findAll().stream()
                .filter(a -> a.getStatus() == AuctionStatus.PAID)
                .mapToDouble(a -> a.getCurrentPrice() * COMMISSION_RATE)
                .sum();
        logger.info("Calculated system total revenue revenue={}", revenue);
        return revenue;
    }
}
