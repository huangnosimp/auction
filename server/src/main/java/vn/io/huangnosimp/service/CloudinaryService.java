package vn.io.huangnosimp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.io.huangnosimp.connection.CloudinaryConnection;
import vn.io.huangnosimp.dto.response.CloudinaryUploadSignatureResponseDTO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

public class CloudinaryService implements ICloudinaryService {
    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);
    private static final String DEFAULT_FOLDER = "auction/products";

    private final CloudinaryConnection cloudinaryConnection;

    public CloudinaryService(CloudinaryConnection cloudinaryConnection) {
        this.cloudinaryConnection = cloudinaryConnection;
    }

    @Override
    public CloudinaryUploadSignatureResponseDTO createUploadSignature(String userId, String requestedFolder) {
        if (!cloudinaryConnection.isConfigured()) {
            logger.error("Cloudinary upload signature rejected because connection is not configured userId={}", userId);
            throw new IllegalStateException("Cloudinary environment variables are not configured");
        }
        if (userId == null || userId.isBlank()) {
            logger.warn("Cloudinary upload signature rejected because user id is missing");
            throw new IllegalArgumentException("User id is required");
        }

        long timestamp = Instant.now().getEpochSecond();
        String folder = normalizeFolder(requestedFolder, userId);

        Map<String, Object> paramsToSign = new TreeMap<>();
        paramsToSign.put("folder", folder);
        paramsToSign.put("timestamp", timestamp);

        String signature = sha1(buildParameterString(paramsToSign) + cloudinaryConnection.getApiSecret());
        logger.info("Cloudinary upload signature created userId={} folder={}", userId, folder);
        return new CloudinaryUploadSignatureResponseDTO(
                cloudinaryConnection.getCloudName(),
                cloudinaryConnection.getApiKey(),
                timestamp,
                folder,
                signature,
                cloudinaryConnection.getImageUploadUrl()
        );
    }

    private String normalizeFolder(String requestedFolder, String userId) {
        if (requestedFolder == null || requestedFolder.isBlank()) {
            return DEFAULT_FOLDER + "/" + userId;
        }

        String sanitized = requestedFolder.trim()
                .replace('\\', '/')
                .replaceAll("/{2,}", "/")
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");

        if (sanitized.isBlank() || sanitized.contains("..")) {
            return DEFAULT_FOLDER + "/" + userId;
        }

        if (sanitized.startsWith(DEFAULT_FOLDER + "/" + userId)) {
            return sanitized;
        }

        return DEFAULT_FOLDER + "/" + userId + "/" + sanitized;
    }

    private String buildParameterString(Map<String, Object> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append("&");
            }
            builder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return builder.toString();
    }

    private String sha1(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 algorithm is not available", e);
        }
    }
}
