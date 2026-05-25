package vn.io.huangnosimp.controller.handler;

import org.junit.jupiter.api.Test;
import vn.io.huangnosimp.controller.MessageRouter;
import vn.io.huangnosimp.dto.response.ServerTimeResponseDTO;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.protocol.ResponseStatus;
import vn.io.huangnosimp.util.GsonParser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class TimeHandlerTest {
    @Test
    void getServerTimeDoesNotRequireLogin() {
        MessageRouter router = new MessageRouter();
        router.registerHandler(ActionType.GET_SERVER_TIME, new TimeHandler.GetServerTimeHandler());
        long before = System.currentTimeMillis();

        Response response = router.route(new Request(ActionType.GET_SERVER_TIME, null), mock(ClientHandle.class));
        long after = System.currentTimeMillis();

        assertEquals(ResponseStatus.SUCCESS, response.getStatus());
        ServerTimeResponseDTO dto = GsonParser.GSON.fromJson(
                GsonParser.GSON.toJsonTree(response.getData()),
                ServerTimeResponseDTO.class
        );
        assertTrue(dto.getServerTimeMillis() >= before);
        assertTrue(dto.getServerTimeMillis() <= after);
    }
}
