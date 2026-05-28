package vn.io.huangnosimp.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudinaryConnection {
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryConnection.class);
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private static volatile CloudinaryConnection instance;

    private CloudinaryConnection() {
        this.cloudName = System.getenv("CLOUDINARY_CLOUD_NAME");
        this.apiKey = System.getenv("CLOUDINARY_API_KEY");
        this.apiSecret = System.getenv("CLOUDINARY_API_SECRET");
        if (isConfigured()) {
            logger.info("Cloudinary connection configured cloudName={}", cloudName);
        } else {
            logger.warn(
                    "Cloudinary environment variables are not fully configured cloudNamePresent={} apiKeyPresent={} apiSecretPresent={}",
                    isPresent(cloudName), isPresent(apiKey), isPresent(apiSecret));
        }
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
