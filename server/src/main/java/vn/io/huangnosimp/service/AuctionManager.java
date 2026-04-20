package vn.io.huangnosimp.service;
//Dùng Singleton pattern
import vn.io.huangnosimp.model.*;
public class AuctionManager {
    private static AuctionManager instance;

    private AuctionManager() {
        // Khởi tạo tài nguyên nếu cần
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    // Các phương thức xử lý logic liên quan đến đấu giá
    public void createAuction(String auctionData) {

    }
}
