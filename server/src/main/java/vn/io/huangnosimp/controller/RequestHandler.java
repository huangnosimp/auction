package vn.io.huangnosimp.controller;

import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;

public interface RequestHandler {
    Response handle(Request request, ClientHandle client);
}
