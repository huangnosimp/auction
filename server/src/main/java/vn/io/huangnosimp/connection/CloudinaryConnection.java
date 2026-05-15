package vn.io.huangnosimp.connection;

public class CloudinaryConnection {
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private static volatile CloudinaryConnection instance;

    private CloudinaryConnection() {
        this.cloudName = System.getenv("CLOUDINARY_CLOUD_NAME");
        this.apiKey = System.getenv("CLOUDINARY_API_KEY");
        this.apiSecret = System.getenv("CLOUDINARY_API_SECRET");
    }

    public static CloudinaryConnection getInstance() {
        if (instance == null) {
            synchronized (CloudinaryConnection.class) {
                if (instance == null) {
                    instance = new CloudinaryConnection();
                }
            }
        }
        return instance;
    }

    public boolean isConfigured() {
        return isPresent(cloudName) && isPresent(apiKey) && isPresent(apiSecret);
    }

    public String getCloudName() {
        return cloudName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public String getImageUploadUrl() {
        return "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
