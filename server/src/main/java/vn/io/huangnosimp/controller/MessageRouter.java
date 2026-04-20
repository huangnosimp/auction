package vn.io.huangnosimp.controller;

import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageRouter {
    private final Map<ActionType, RequestHandler> handlers;

    public MessageRouter() {
        this.handlers = new ConcurrentHashMap<>();
    }

    public void registerHandler(ActionType type, RequestHandler handler) {
        handlers.put(type, handler);
    }

    public Response route(Request request, ClientHandle client) {
        ActionType action = request.getAction();

        if (action == null) {
            return new Response(ResponseStatus.ERROR, "Invalid request: missing action field");
        }
        if (action != ActionType.LOGIN && action != ActionType.REGISTER) {
            if (client.getUserId() == null) {
                return new Response(ResponseStatus.UNAUTHORIZED, "Unauthorized: please login first");
            }
        }
        System.out.println("[Router] Routing package: " + action + " for user: " + client.getUserId());
        RequestHandler handler = handlers.get(action);
        if (handler != null) {
            return handler.handle(request, client);
        }
        return new Response(ResponseStatus.ERROR, "Action not supported: " + action);
    }
}
