package vn.io.huangnosimp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.huangnosimp.dto.response.LoginResult;
import vn.io.huangnosimp.dto.response.RegisterResult;
import vn.io.huangnosimp.dto.response.TransactionResult;
import vn.io.huangnosimp.enums.UserType;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.network.ClientHandle;
import vn.io.huangnosimp.repository.IUserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static vn.io.huangnosimp.TestFixtures.bannedMember;
import static vn.io.huangnosimp.TestFixtures.member;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private IUserRepository userRepository;

    @Mock
    private ClientHandle client;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void registerRejectsInvalidEmailWithoutSaving() {
        RegisterResult result = userService.register(UserType.MEMBER, "alice", "secret", "invalid-email");

        assertEquals(RegisterResult.INVALID_EMAIL, result);
        verify(userRepository, never()).saveUser(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void registerHashesPasswordBeforeSavingMember() {
        when(userRepository.checkUsername("alice")).thenReturn(false);
        when(userRepository.checkEmail("alice@example.com")).thenReturn(false);
        when(userRepository.saveUser(anyString(), eq("alice"), anyString(), eq("alice@example.com"), eq("MEMBER")))
                .thenReturn(true);

        RegisterResult result = userService.register(UserType.MEMBER, "alice", "secret", "alice@example.com");

        assertEquals(RegisterResult.SUCCESS, result);
        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).saveUser(anyString(), eq("alice"), passwordCaptor.capture(), eq("alice@example.com"), eq("MEMBER"));
        assertTrue(BCrypt.checkpw("secret", passwordCaptor.getValue()));
    }

    @Test
    void loginSuccessSetsClientIdentity() {
        Member alice = member("user-1", "alice", 100.0, 0.0);
        alice.setPassword(BCrypt.hashpw("secret", BCrypt.gensalt()));
        when(userRepository.findByUsername("alice")).thenReturn(alice);

        LoginResult result = userService.login(UserType.MEMBER, "alice", "secret", client);

        assertEquals(LoginResult.SUCCESS, result);
        verify(client).setUserId("user-1");
        verify(client).setUserType(UserType.MEMBER);
    }

    @Test
    void loginRejectsBannedMemberAndClearsClientIdentity() {
        Member banned = bannedMember("user-1", "alice", LocalDateTime.now().plusDays(1));
        banned.setPassword(BCrypt.hashpw("secret", BCrypt.gensalt()));
        when(userRepository.findByUsername("alice")).thenReturn(banned);

        LoginResult result = userService.login(UserType.MEMBER, "alice", "secret", client);

        assertEquals(LoginResult.BANNED, result);
        verify(client).setUserId(null);
        verify(client).setUserType(null);
    }

    @Test
    void loginClearsExpiredBanBeforeSucceeding() {
        Member banned = bannedMember("user-1", "alice", LocalDateTime.now().minusDays(1));
        banned.setPassword(BCrypt.hashpw("secret", BCrypt.gensalt()));
        when(userRepository.findByUsername("alice")).thenReturn(banned);

        LoginResult result = userService.login(UserType.MEMBER, "alice", "secret", client);

        assertEquals(LoginResult.SUCCESS, result);
        verify(userRepository).updateBanStatus("user-1", false, null);
        verify(client).setUserId("user-1");
    }

    @Test
    void depositUpdatesMemberBalance() {
        when(userRepository.findById("user-1")).thenReturn(member("user-1", "alice", 100.0, 0.0));
        when(userRepository.updateBalance("user-1", 150.0)).thenReturn(true);

        TransactionResult result = userService.deposit("user-1", 50.0);

        assertEquals(TransactionResult.SUCCESS, result);
        verify(userRepository).updateBalance("user-1", 150.0);
    }

    @Test
    void withdrawRejectsInsufficientFunds() {
        when(userRepository.findById("user-1")).thenReturn(member("user-1", "alice", 100.0, 0.0));

        TransactionResult result = userService.withdraw("user-1", 150.0);

        assertEquals(TransactionResult.INSUFFICIENT_FUNDS, result);
        verify(userRepository, never()).updateBalance(anyString(), anyDouble());
    }
}
