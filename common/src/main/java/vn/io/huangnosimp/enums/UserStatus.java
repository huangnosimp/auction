package vn.io.huangnosimp.enums;

public enum UserStatus {
    ONLINE("Online"),
    OFFLINE("Offline"),
    BANNED("Banned");

    private final String displayName;
    UserStatus(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}