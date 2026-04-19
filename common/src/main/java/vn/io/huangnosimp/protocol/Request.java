package vn.io.huangnosimp.protocol;

public class Request {
    private ActionType action;
    private Object data;
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
}
