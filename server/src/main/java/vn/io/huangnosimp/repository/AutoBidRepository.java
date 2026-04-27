package vn.io.huangnosimp.repository;

import vn.io.huangnosimp.model.AutoBidConfig;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AutoBidRepository implements IAutoBidRepository {

    private final Map<String, List<AutoBidConfig>> storage = new ConcurrentHashMap<>();

    @Override
    public void save(AutoBidConfig config) {
        String auctionId = config.getAuction().getId();
        storage.computeIfAbsent(auctionId, k -> Collections.synchronizedList(new ArrayList<>()));
        List<AutoBidConfig> configs = storage.get(auctionId);
        configs.removeIf(c -> c.getBidder().getId().equals(config.getBidder().getId()));
        configs.add(config);
        System.out.println("[Repo] Saved AutoBid config for user: " + config.getBidder().getUsername());
    }

    @Override
    public List<AutoBidConfig> findByAuctionId(String auctionId) {
        List<AutoBidConfig> configs = storage.get(auctionId);
        return (configs != null) ? new ArrayList<>(configs) : new ArrayList<>();
    }

    @Override
    public AutoBidConfig findByMemberAndAuction(String memberId, String auctionId) {
        List<AutoBidConfig> configs = storage.get(auctionId);
        if (configs == null) return null;

        return configs.stream()
                .filter(c -> c.getBidder().getId().equals(memberId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void delete(String memberId, String auctionId) {
        List<AutoBidConfig> configs = storage.get(auctionId);
        if (configs != null) {
            configs.removeIf(c -> c.getBidder().getId().equals(memberId));
        }
    }
}