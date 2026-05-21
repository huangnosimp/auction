package vn.io.huangnosimp.util;

import vn.io.huangnosimp.dto.response.AuctionAdminDTO;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.dto.response.MemberDTO;
import vn.io.huangnosimp.model.User;
import vn.io.huangnosimp.repository.IUserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

        LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochMilli(auction.getStartTime()), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(Instant.ofEpochMilli(auction.getEndTime()), ZoneId.systemDefault());

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
                start,
                end,
                statusStr,
                winnerName
        );
    }

    public static List<AuctionAdminDTO> toAuctionAdminDTOList(List<Auction> auctions) {
        return auctions.stream()
                .map(ModelMapper::toAuctionAdminDTO)
                .collect(Collectors.toList());
    }
}