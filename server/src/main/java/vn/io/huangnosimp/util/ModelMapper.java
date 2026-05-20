package vn.io.huangnosimp.util;

import vn.io.huangnosimp.dto.response.AuctionCardDTO;
import vn.io.huangnosimp.model.Auction;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.dto.response.MemberDTO;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ModelMapper {

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

    public static AuctionCardDTO toAuctionCardDTO(Auction auction) {
        if (auction == null) return null;

        return new AuctionCardDTO(
                auction.getId(),
                auction.getItem().getName(),
                auction.getCurrentPrice(),
                0.0,
                auction.getStartTime(),
                auction.getEndTime(),
                0,
                0,
                null
        );
    }

    public static List<AuctionCardDTO> toAuctionCardDTOList(List<Auction> auctions) {
        return auctions.stream()
                .map(ModelMapper::toAuctionCardDTO)
                .collect(Collectors.toList());
    }
}