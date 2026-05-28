package vn.io.huangnosimp.dto.request;

public class CloudinaryUploadSignatureRequestDTO {
    private final String folder;

    public CloudinaryUploadSignatureRequestDTO() {
        this.folder = null;
    }

    public CloudinaryUploadSignatureRequestDTO(String folder) {
        this.folder = folder;
    }

    public String getFolder() {
        return folder;
    }
}
