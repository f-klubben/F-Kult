package dk.fklub.fkult;

import dk.fklub.fkult.business.services.Authenticator;
import dk.fklub.fkult.persistence.repository.AuthRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.Null;

import java.io.File;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AuthenticatorTest {
    @Mock
    private AuthRepository authRepository;
    private Authenticator authenticator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        authenticator = new Authenticator(authRepository);
    }

    @Test
    @DisplayName("Test userExistLocally return true")
    void testUserExistsLocally() throws Exception {
        when(authRepository.userExistsLocally("test")).thenReturn(true);

        boolean result = authenticator.receiveUsername("test");

        assertTrue(result);
        verify(authRepository, never()).fetchMemberId(anyString());
        verify(authRepository, never()).fetchMemberInfo(any());
        verify(authRepository, never()).upsertUser(anyString(), anyString());
    }

    @Test
    @DisplayName("test stregsystem API return true")
    void testStegsystemAPI() {
        // Arrange
        when(authRepository.userExistsLocally("test")).thenReturn(false);
        when(authRepository.fetchMemberId("test")).thenReturn(4904);
        AuthRepository.MemberInfo info = new AuthRepository.MemberInfo();
        info.balance = 50;
        info.username = "test";
        info.active = true;
        info.name = "Testy McTestface";
        when(authRepository.fetchMemberInfo(4904)).thenReturn(info);
        when(authRepository.upsertUser("Testy McTestface", "test")).thenReturn(1);

        // Act
        boolean result = authenticator.receiveUsername("test");

        // Assert
        assertTrue(result);
        verify(authRepository).userExistsLocally("test");
        verify(authRepository).fetchMemberId("test");
        verify(authRepository).fetchMemberInfo(4904);
        verify(authRepository).upsertUser("Testy McTestface", "test");
        verifyNoMoreInteractions(authRepository);
    }

    @Test
    @DisplayName("test fetchMemberID return False")
    void testFetchMemberIDInvalid() {
        // Arrange
        when(authRepository.userExistsLocally("test")).thenReturn(false);
        when(authRepository.fetchMemberId("test")).thenReturn(null);

        // Act
        boolean result = authenticator.receiveUsername("test");

        // Assert
        assertFalse(result);
        verify(authRepository).userExistsLocally("test");
        verify(authRepository).fetchMemberId("test");
        verifyNoMoreInteractions(authRepository);
    }

    @Test
    @DisplayName("testing fetchMemberInfo return false")
    void testFetchMemberInfoInvalid() {
        // Arrange
        when(authRepository.userExistsLocally("test")).thenReturn(false);
        when(authRepository.fetchMemberId("test")).thenReturn(4904);
        AuthRepository.MemberInfo info = new AuthRepository.MemberInfo();
        info.balance = null;
        info.username = null;
        info.active = null;
        info.name = null;
        when(authRepository.fetchMemberInfo(4904)).thenReturn(info);

        // Act
        boolean result = authenticator.receiveUsername("test");

        // Assert
        assertFalse(result);
        verify(authRepository).userExistsLocally("test");
        verify(authRepository).fetchMemberId("test");
        verify(authRepository).fetchMemberInfo(4904);
        verify(authRepository, never()).upsertUser(anyString(), anyString());
    }

    @Test
    @DisplayName("testing upsertUser return 0")
    void testUpsertUserInvalid() {
        // Arrange
        when(authRepository.userExistsLocally("test")).thenReturn(false);
        when(authRepository.fetchMemberId("test")).thenReturn(4904);
        AuthRepository.MemberInfo info = new AuthRepository.MemberInfo();
        info.balance = 50;
        info.username = "test";
        info.active = true;
        info.name = "Testy McTestface";
        when(authRepository.fetchMemberInfo(4904)).thenReturn(info);
        when(authRepository.upsertUser("Testy McTestface", "test")).thenReturn(0);

        // Act
        boolean result = authenticator.receiveUsername("test");

        // Assert
        assertFalse(result);
        verify(authRepository).userExistsLocally("test");
        verify(authRepository).fetchMemberId("test");
        verify(authRepository).fetchMemberInfo(4904);
        verify(authRepository).upsertUser("Testy McTestface", "test");
        verifyNoMoreInteractions(authRepository);
    }
}
