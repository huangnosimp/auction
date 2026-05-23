package vn.io.huangnosimp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import vn.io.huangnosimp.Manager.AuctionCountdownUtil;
import vn.io.huangnosimp.Manager.FormatUtil;
import vn.io.huangnosimp.dto.response.AuctionCardDTO;

import java.util.List;

public class RowItemController {
    @FXML private ImageView imgProduct;
    @FXML private Label lblProductName;
    
    // Fields for Won Item Row
    @FXML private Label lblPaidPrice;
    @FXML private Label lblWonAt;

    // Fields for Listing Row
    @FXML private Label lblBidCount;
    @FXML private Label lblTopBid;
    @FXML private Label lblEndsIn;
    @FXML private Label lblEndDate;
    @FXML private Button btnAction;

    private String auctionId;
    private List<String> displayImg;

    public void setUpWonRow(AuctionCardDTO dto) {
        this.auctionId = dto.getAuctionId();
        this.displayImg = dto.getImageUrl();

        if (displayImg != null && !displayImg.isEmpty()) {
            imgProduct.setImage(new Image(displayImg.get(0), 0, 0, true, true));
        } else {
            imgProduct.setImage(null);
        }

        if (lblProductName != null) {
            lblProductName.setText(dto.getProductName());
        }

        if (lblPaidPrice != null) {
            lblPaidPrice.setText(FormatUtil.formatNumber(dto.getYourBid()));
        }

        if (lblWonAt != null) {
            lblWonAt.setText(AuctionCountdownUtil.formatEpochSecond(dto.getEndTime()));
        }
    }

    public void setUpListingRow(AuctionCardDTO dto) {
        this.auctionId = dto.getAuctionId();
        this.displayImg = dto.getImageUrl();

        if (displayImg != null && !displayImg.isEmpty()) {
            imgProduct.setImage(new Image(displayImg.get(0), 0, 0, true, true));
        } else {
            imgProduct.setImage(null);
        }

        if (lblProductName != null) {
            lblProductName.setText(dto.getProductName());
        }

        if (lblBidCount != null) {
            lblBidCount.setText(String.valueOf(dto.getBidCount()));
        }

        if (lblTopBid != null) {
            lblTopBid.setText(FormatUtil.formatNumber(dto.getCurrentPrice()));
        }

        if (lblEndsIn != null) {
            AuctionCountdownUtil countdown = new AuctionCountdownUtil(lblEndsIn, dto.getStartTime(), dto.getEndTime());
            countdown.start();
        }

        if (lblEndDate != null) {
            lblEndDate.setText(AuctionCountdownUtil.formatEpochSecond(dto.getEndTime()));
        }
    }
}
