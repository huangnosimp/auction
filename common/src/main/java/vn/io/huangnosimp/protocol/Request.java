package vn.io.huangnosimp.protocol;

import java.util.UUID;

public class Request {
    private String requestId;
    private final ActionType action;
    private final Object data;

    public Request(ActionType action, Object data) {
        this.requestId = UUID.randomUUID().toString();
        this.action = action;
        this.data = data;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public ActionType getAction() {
        return action;
    }

    public Object getData() {
        return data;
    }
}
