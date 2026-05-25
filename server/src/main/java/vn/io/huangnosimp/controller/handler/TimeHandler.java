package vn.io.huangnosimp.controller.handler;

import vn.io.huangnosimp.controller.RequestHandler;
import vn.io.huangnosimp.dto.response.ServerTimeResponseDTO;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.protocol.ResponseStatus;

public class TimeHandler {
    public static class GetServerTimeHandler implements RequestHandler {
        @Override
        public Response handle(Request request, ClientHandle client) {
            return new Response(
                    ResponseStatus.SUCCESS,
                    "Server time",
                    new ServerTimeResponseDTO(System.currentTimeMillis())
            );
        }
    }
}
