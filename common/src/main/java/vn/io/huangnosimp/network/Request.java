package vn.io.huangnosimp.network;

import com.google.gson.Gson;
import vn.io.huangnosimp.model.ActionType;

public class Request {
    private ActionType action;
    private Object data;
    public static final Gson GSON = new Gson();
    public Request() {}
    public Request (ActionType action, Object data) {
        this.action = action;
        this.data = data;
    }

    public ActionType getAction() {
        return action;
    }

    public Object getData() {
        return data;
    }
    public String toJson() {
        return GSON.toJson(this);
    }
    public static Request fromJson(String json) {
        return GSON.fromJson(json, Request.class);
    }
}
