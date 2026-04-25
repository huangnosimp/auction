package vn.io.huangnosimp.dto.request;

public class TargetIdRequestDTO {
    private String targetId;

    public TargetIdRequestDTO() {
    }

    public TargetIdRequestDTO(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
}