package vn.io.huangnosimp.model;

public enum ItemType {
    ART,
    ELECTRONICS,
    VEHICLE,
    GENERIC,
    UNKNOWN;
    public static ItemType fromString(String typeStr) {
        if (typeStr == null || typeStr.trim().isEmpty()) {
            return UNKNOWN;
        }
        try {
            return ItemType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Cảnh báo: CSDL chứa loại hàng không xác định: " + typeStr);
            return UNKNOWN;
        }
    }
}
