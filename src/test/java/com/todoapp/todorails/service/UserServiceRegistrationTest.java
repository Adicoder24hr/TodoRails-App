package com.todoapp.todorails.service;

import com.todoapp.todorails.model.User;
import com.todoapp.todorails.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceRegistrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void encodesPasswordAndPersistsNewUser() {
        User user = new User("new-user", "new@example.com", "test-password", "USER");
        when(userRepository.findByUsername("new-user")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("test-password")).thenReturn("encoded-value");
        when(userRepository.save(user)).thenReturn(user);

        User saved = userService.registerUser(user);

        assertEquals(user, saved);
        assertEquals("encoded-value", saved.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void duplicateUsernameIsRejectedBeforeEmailLookupOrSave() {
        User user = new User("existing-user", "new@example.com", "test-password", "USER");
        when(userRepository.findByUsername("existing-user")).thenReturn(Optional.of(new User()));

        DuplicateRegistrationException exception = assertThrows(
                DuplicateRegistrationException.class, () -> userService.registerUser(user));

        assertEquals("username", exception.getField());
        verify(userRepository, never()).save(user);
    }

    @Test
    void duplicateEmailIsRejectedBeforePasswordEncodingOrSave() {
        User user = new User("new-user", "existing@example.com", "test-password", "USER");
        when(userRepository.findByUsername("new-user")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        DuplicateRegistrationException exception = assertThrows(
                DuplicateRegistrationException.class, () -> userService.registerUser(user));

        assertEquals("email", exception.getField());
        verify(passwordEncoder, never()).encode("test-password");
        verify(userRepository, never()).save(user);
    }
}
