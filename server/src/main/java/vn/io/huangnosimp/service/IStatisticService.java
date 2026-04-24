package vn.io.huangnosimp.service;

import vn.io.huangnosimp.dto.response.DashboardResponseDTO;
import vn.io.huangnosimp.repository.*;

public interface IStatisticService {
    DashboardResponseDTO getDashboardStatistics(String userId);
}
