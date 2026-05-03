package vn.io.huangnosimp.dto.response;

public class MemberDTO {
    private String id;
    private String username;
    private String email;
    private boolean isBanned;

    // Constructor
    public MemberDTO(String id, String username, String email, boolean isBanned) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.isBanned = isBanned;
    }

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public boolean isBanned() { return isBanned; }
}