package vn.io.huangnosimp.dto.response;

public class CloudinaryUploadSignatureResponseDTO {
    private final String cloudName;
    private final String apiKey;
    private final long timestamp;
    private final String folder;
    private final String signature;
    private final String uploadUrl;

    public CloudinaryUploadSignatureResponseDTO(String cloudName, String apiKey, long timestamp, String folder,
                                                String signature, String uploadUrl) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.timestamp = timestamp;
        this.folder = folder;
        this.signature = signature;
        this.uploadUrl = uploadUrl;
    }

    public String getCloudName() {
        return cloudName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFolder() {
        return folder;
    }

    public String getSignature() {
        return signature;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }
}
