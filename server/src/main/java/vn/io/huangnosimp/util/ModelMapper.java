package vn.io.huangnosimp.util;

import vn.io.huangnosimp.dto.response.AuctionAdminDTO;
import vn.io.huangnosimp.dto.response.AutoBidResponseDTO;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.AutoBidConfig;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.dto.response.MemberDTO;
import vn.io.huangnosimp.model.User;
import vn.io.huangnosimp.repository.IUserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModelMapper {

    private static IUserRepository userRepository;

    public static void setUserRepository(IUserRepository userRepo) {
        ModelMapper.userRepository = userRepo;
    }

    public static MemberDTO toMemberDTO(Member member) {
        if (member == null) return null;

        String banUntilString = null;
        if (member.getBanUntil() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            banUntilString = member.getBanUntil().format(formatter);
        }

        return new MemberDTO(
                member.getId(),
                member.getUsername(),
                member.getEmail(),
                member.getAccountBalance(),
                member.getFrozenBalance(),
                member.isBanned(),
                banUntilString
        );
    }

    public static List<MemberDTO> toMemberDTOList(List<Member> members) {
        return members.stream()
                .map(ModelMapper::toMemberDTO)
                .collect(Collectors.toList());
    }

    public static AuctionAdminDTO toAuctionAdminDTO(Auction auction) {
        if (auction == null) return null;

        DateTimeFormatter TimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime startLocal = LocalDateTime.ofInstant(Instant.ofEpochMilli(auction.getStartTime()), ZoneId.systemDefault());
        LocalDateTime endLocal = LocalDateTime.ofInstant(Instant.ofEpochMilli(auction.getEndTime()), ZoneId.systemDefault());

        String startTimeStr = (startLocal != null) ? startLocal.format(TimeFormatter) : "N/A";
        String endTimeStr = (endLocal != null) ? endLocal.format(TimeFormatter) : "N/A";

        String sellerName = (auction.getSeller() != null) ? auction.getSeller().getUsername() : "N/A";

        String statusStr = (auction.getStatus() != null) ? auction.getStatus().name() : "UNKNOWN";

        String winnerName = "None";
        String winnerId = auction.getCurrentWinnerId();

        if (winnerId != null && !winnerId.trim().isEmpty() && userRepository != null) {
            User winner = userRepository.findById(winnerId);
            if (winner != null) {
                winnerName = winner.getUsername();
            } else {
                winnerName = "ID: " + winnerId;
            }
        }

        return new AuctionAdminDTO(
                auction.getId(),
                auction.getItem() != null ? auction.getItem().getName() : "N/A",
                sellerName,
                auction.getStartPrice(),
                auction.getCurrentPrice(),
                startTimeStr,
                endTimeStr,
                statusStr,
                winnerName
        );
    }

    public static List<AuctionAdminDTO> toAuctionAdminDTOList(List<Auction> auctions) {
        return auctions.stream()
                .map(ModelMapper::toAuctionAdminDTO)
                .collect(Collectors.toList());
    }

    public static AutoBidResponseDTO toAutoBidResponseDTO(AutoBidConfig config) {
        if (config == null) return null;

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String registeredAtStr = (config.getRegisteredAt() != null)
                ? config.getRegisteredAt().format(timeFormatter)
                : "N/A";

        return new AutoBidResponseDTO(
                config.getId(),
                config.getMaxBid(),
                config.getIncrement(),
                config.getAuction().getId(),
                registeredAtStr
        );
    }

    public static List<AutoBidResponseDTO> toAutoBidResponseDTOList(List<AutoBidConfig> configs) {
        if (configs == null) {
            return new ArrayList<>();
        }

        return configs.stream()
                .map(ModelMapper::toAutoBidResponseDTO)
                .collect(Collectors.toList());
    }
}