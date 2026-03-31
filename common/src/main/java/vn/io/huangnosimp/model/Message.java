package vn.io.huangnosimp.model;
import com.google.gson.Gson;
public class Message {
    private String action;
    private String data;

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
        return new Gson().toJson(this);
    }
    public static Message fromJson(String json) {
        return new Gson().fromJson(json, Message.class);
    }
}
