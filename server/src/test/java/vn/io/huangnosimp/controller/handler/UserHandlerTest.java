package vn.io.huangnosimp.controller.handler;

import org.junit.jupiter.api.Test;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.network.ClientSessionManager;
import vn.io.huangnosimp.protocol.ActionType;
import vn.io.huangnosimp.protocol.Request;
import vn.io.huangnosimp.protocol.Response;
import vn.io.huangnosimp.protocol.ResponseStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserHandlerTest {
    @Test
    void logoutLeavesAllAuctionRooms() {
        ClientSessionManager sessionManager = ClientSessionManager.getInstance();
        ClientHandle client = mock(ClientHandle.class);
        String userId = "user-" + UUID.randomUUID();
        String auctionId = "auction-" + UUID.randomUUID();

        when(client.getUserId()).thenReturn(userId);
        sessionManager.joinRoom(auctionId, client);
        assertTrue(sessionManager.isUserInRoom(auctionId, userId));

        Response response = new UserHandler.LogoutHandler().handle(new Request(ActionType.LOGOUT, null), client);

        assertEquals(ResponseStatus.SUCCESS, response.getStatus());
        assertFalse(sessionManager.isUserInRoom(auctionId, userId));
        verify(client).setUserId(null);
        verify(client).setUserType(null);
    }
}
