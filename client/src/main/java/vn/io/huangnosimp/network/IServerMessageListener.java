package vn.io.huangnosimp.network;

import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;

public interface IServerMessageListener {
    void onResponseReceived(Response response);
    void onRequestReceived(Request notification);
    void onDisconnected(String reason);
}
