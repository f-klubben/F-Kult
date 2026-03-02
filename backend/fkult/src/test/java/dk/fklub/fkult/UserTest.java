package dk.fklub.fkult;

import dk.fklub.fkult.business.services.Authenticator;
import dk.fklub.fkult.business.services.UserService;
import dk.fklub.fkult.persistence.entities.User;
import dk.fklub.fkult.persistence.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private Authenticator auth;

    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, auth);
    }

    @Test
    @DisplayName("Get all users from repo")
    void testGetAllUsers(){
        // Arrange
        List<User> mockUsers = Arrays.asList(
                new User(1, "test", "Test Person", 0, 0),
                new User(2, "tset", "Other Test Person", 0, 1)
        );
        when(userRepository.findAll()).thenReturn(mockUsers);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertEquals("test", result.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Get existing user from repo")
    void testGetExistingUser(){
        // Arrange
        User user = new User(1, "test", "Test Person", 0, 1);
        when(userRepository.findUser("test")).thenReturn(user);

        // Act
        User result = userService.getUser("test");

        // Assert
        assertNotNull(result);
        assertEquals("test", result.getUsername());
        verify(userRepository).findUser("test");
    }

    @Test
    @DisplayName("Get nonexisting user from repo")
    void testGetNonExistingUser(){
        // Act
        User result = userService.getUser("test");

        // Assert
        assertNull(result);
        verify(userRepository).findUser("test");
    }

    @Test
    @DisplayName("Attempt to make user admin")
    void testUpdateAdmin(){
        // Arrange
        User user = new User(1, "test", "Test Person", 0, 1);
        when(userRepository.updateAdminStatus("test", 1)).thenReturn(user);
        when(auth.receiveUsername("test")).thenReturn(true);

        // Act
        ResponseEntity expected = ResponseEntity.ok("User successfully admin");
        ResponseEntity<?> result = userService.postAdminUser("test", 1);

        // Assert
        assertEquals(expected, result);
        assertEquals(1, user.getAdmin());
        verify(userRepository).updateAdminStatus("test", 1);
    }

    @Test
    @DisplayName("Fail to make user admin")
    void testUpdateAdminFail(){
        // Arrange
        User user = new User(1, "test", "Test Person", 0, 0);
        when(auth.receiveUsername("test")).thenReturn(true);
        when(userRepository.updateAdminStatus("test", 1)).thenReturn(user);

        // Act
        ResponseEntity expected = ResponseEntity.status(500).body("User not correctly updated");
        ResponseEntity<?> result = userService.postAdminUser("test", 1);

        // Assert
        assertEquals(expected, result);
        assertEquals(0, user.getAdmin());
        verify(userRepository).updateAdminStatus("test", 1);
    }


    @Test
    @DisplayName("ban unbanned user")
    void testUpdateUserBan(){
        // Arrange
        when(auth.receiveUsername("test")).thenReturn(true);
        when(userService.getIfUserBanned("test")).thenReturn(false);

        User after = new User(1, "test", "Test Person", 1, 0);
        when(userRepository.updateUserBanStatus("test", 1)).thenReturn(after);

        // Act
        ResponseEntity expected = ResponseEntity.ok("User successfully banned");
        ResponseEntity<?> result = userService.postUserBan("test", 1);

        // Assert
        assertEquals(expected, result);
        verify(userRepository).updateUserBanStatus("test", 1);
    }

    @Test
    @DisplayName("Check if a banned user is banned")
    void testCheckBan() {
        // Arrange
        when(userRepository.findIfUserBanned("test")).thenReturn(true);

        // Act
        boolean result = userService.getIfUserBanned("test");

        // Assert
        assertTrue(result);
        verify(userRepository).findIfUserBanned("test");
    }
}