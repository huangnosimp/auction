package vn.io.huangnosimp.dto.response;

import java.util.List;

public class GetPostedAuctionCardResponseDTO {
    private List<AuctionCardDTO> auctionCards;
    public GetPostedAuctionCardResponseDTO(List<AuctionCardDTO> auctionCards) {
        this.auctionCards = auctionCards;
    }
    public List<AuctionCardDTO> getAuctionCards() {
        return auctionCards;
    }
}
