package vn.io.huangnosimp.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.io.huangnosimp.model.Admin;
import vn.io.huangnosimp.model.Member;
import vn.io.huangnosimp.model.User;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTest extends H2RepositoryTestSupport {
    private UserRepository userRepository;

    @BeforeEach
    void setUpRepository() {
        userRepository = new UserRepository(databaseConnection);
    }

    @Test
    void saveUserAndFindMemberByUsername() {
        assertTrue(userRepository.saveUser("user-1", "alice", "hash", "alice@example.com", "MEMBER"));

        User user = userRepository.findByUsername("alice");

        assertInstanceOf(Member.class, user);
        assertEquals("user-1", user.getId());
        assertEquals("alice@example.com", user.getEmail());
        assertTrue(userRepository.checkUsername("alice"));
        assertTrue(userRepository.checkEmail("alice@example.com"));
    }

    @Test
    void findByIdMapsAdmin() {
        assertTrue(userRepository.saveUser("admin-1", "admin", "hash", "admin@example.com", "ADMIN"));

        User user = userRepository.findById("admin-1");

        assertInstanceOf(Admin.class, user);
        assertEquals("admin", user.getUsername());
    }

    @Test
    void updateBalancesAndBanStatus() {
        userRepository.saveUser("user-1", "alice", "hash", "alice@example.com", "MEMBER");

        assertTrue(userRepository.updateBalance("user-1", 250.0));
        assertTrue(userRepository.updateFrozenBalance("user-1", 75.0));
        assertTrue(userRepository.updateBanStatus("user-1", true, LocalDateTime.of(2030, 1, 1, 0, 0)));

        Member user = (Member) userRepository.findById("user-1");
        assertEquals(250.0, user.getAccountBalance());
        assertEquals(75.0, user.getFrozenBalance());
        assertTrue(user.isBanned());
        assertEquals(LocalDateTime.of(2030, 1, 1, 0, 0), user.getBanUntil());
    }

    @Test
    void findMethodsReturnNullOrFalseWhenMissing() throws SQLException {
        assertNull(userRepository.findById("missing"));
        assertFalse(userRepository.checkUsername("missing"));
        assertFalse(userRepository.checkEmail("missing@example.com"));
        assertEquals(0, countRows("Users"));
    }
}
