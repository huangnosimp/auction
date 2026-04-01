package vn.io.huangnosimp.model;
import com.google.gson.Gson;
public class Message {
    private String action;
    private String data;
    public static final Gson GSON = new Gson();

    public Message(String action, String data) {
        this.action = action;
        this.data = data;
    }

    public String getAction() {
        return action;
    }

    public String getData() {
        return data;
    }

    public String toJson() {
        return GSON.toJson(this);
    }
    public static Message fromJson(String json) {
        return GSON.fromJson(json, Message.class);
    }
}
