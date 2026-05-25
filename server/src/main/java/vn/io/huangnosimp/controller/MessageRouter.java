package vn.io.huangnosimp.controller;

import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageRouter {
    private static final Logger logger = LoggerFactory.getLogger(MessageRouter.class);
    private final Map<ActionType, RequestHandler> handlers;

    public MessageRouter() {
        this.handlers = new ConcurrentHashMap<>();
    }

    public void registerHandler(ActionType type, RequestHandler handler) {
        handlers.put(type, handler);
    }

    public Response route(Request request, ClientHandle client) {
        if (request == null) {
            logger.warn("Invalid request: request body is null");
            return new Response(ResponseStatus.ERROR, "Invalid request");
        }

        ActionType action = request.getAction();
        putMdc(request, client, action);

        try {
            if (action == null) {
                logger.warn("Invalid request: missing action field");
                return new Response(ResponseStatus.ERROR, "Invalid request: missing action field");
            }
            if (action != ActionType.LOGIN
                    && action != ActionType.REGISTER
                    && action != ActionType.GET_SERVER_TIME) {
                if (client.getUserId() == null) {
                    logger.warn("Rejected unauthorized request");
                    return new Response(ResponseStatus.UNAUTHORIZED, "Unauthorized: please login first");
                }
            }
            RequestHandler handler = handlers.get(action);
            if (handler != null) {
                logger.info("Routing request");
                return handler.handle(request, client);
            }
            logger.warn("Unsupported action");
            return new Response(ResponseStatus.ERROR, "Action not supported: " + action);
        } finally {
            MDC.clear();
        }
    }

    private void putMdc(Request request, ClientHandle client, ActionType action) {
        if (request.getRequestId() != null) {
            MDC.put("requestId", request.getRequestId());
        }
        if (client != null && client.getUserId() != null) {
            MDC.put("userId", client.getUserId());
        }
        if (action != null) {
            MDC.put("action", action.name());
        }
    }
}
