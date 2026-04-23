package vn.io.huangnosimp.protocol;

public class Response {
    private String requestId;
    private final ResponseStatus status;
    private final String message;
    private final Object data;

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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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
}
