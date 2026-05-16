package vn.io.huangnosimp.dto.response;

public class MemberDTO {
    private String id;
    private String username;
    private String email;
    private boolean isBanned;
    private String banUntil;
    private String status;

    public MemberDTO(String id, String username, String email, boolean isBanned, String banUntil) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.isBanned = isBanned;
        this.banUntil = banUntil;
        this.status = isBanned ? "Bị Ban" : "Offline";
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public boolean isBanned() { return isBanned; }

    public String getBanUntil() { return banUntil; }
    public void setBanUntil(String banUntil) { this.banUntil = banUntil; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}