package vn.io.huangnosimp.util;

public final class SensitiveDataMasker {
    private SensitiveDataMasker() {
    }

    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex == email.length() - 1) {
            return "***";
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);
        String visiblePrefix = localPart.substring(0, 1);
        return visiblePrefix + "***@" + domain;
    }

    public static String maskSecret(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return "***";
    }
}
