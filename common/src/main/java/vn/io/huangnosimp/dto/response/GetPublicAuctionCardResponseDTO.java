package vn.io.huangnosimp.dto.response;

import java.util.List;

public class GetPublicAuctionCardResponseDTO {
    private final List<AuctionCardDTO> publicAuctionCardList;
    public GetPublicAuctionCardResponseDTO(List<AuctionCardDTO> publicAuctionCardList) {
        this.publicAuctionCardList = publicAuctionCardList;
    }
    public List<AuctionCardDTO> getPublicAuctionCardList() {
        return publicAuctionCardList;
    }
}
