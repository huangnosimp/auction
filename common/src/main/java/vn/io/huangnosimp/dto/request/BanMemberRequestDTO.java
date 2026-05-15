package vn.io.huangnosimp.dto.request;

public class BanMemberRequestDTO {
    private String targetId;
    private int durationMinutes;

    public BanMemberRequestDTO() {}

    public BanMemberRequestDTO(String targetId, int durationMinutes) {
        this.targetId = targetId;
        this.durationMinutes = durationMinutes;
    }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
}