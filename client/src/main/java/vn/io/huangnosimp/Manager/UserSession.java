package vn.io.huangnosimp.Manager;

import vn.io.huangnosimp.dto.response.DashboardResponseDTO;

public class UserSession {
    private static DashboardResponseDTO dashboardInfo;
    public static void setDashboardInfo(DashboardResponseDTO info) {
        dashboardInfo = info;
    }
    public static DashboardResponseDTO getDashboardInfo() {
        return dashboardInfo;
    }
}
