package vn.io.huangnosimp.network;

import com.google.gson.Gson;
import vn.io.huangnosimp.model.ResponseStatus;

public class Response {
    private ResponseStatus status;
    private String message;
    private Object data;
    public static final Gson GSON = new Gson();

    public Response() {
    }
    public Response(ResponseStatus status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
    public Response(ResponseStatus status, String message) {
        this.status = status;
        this.message = message;
        this.data = null;
    }
    public Response(ResponseStatus status, Object data) {
        this.status = status;
        this.message = null;
        this.data = data;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
    public String toJson() {
        return GSON.toJson(this);
    }
    public static Response fromJson(String json) {
        return GSON.fromJson(json, Response.class);
    }
}
