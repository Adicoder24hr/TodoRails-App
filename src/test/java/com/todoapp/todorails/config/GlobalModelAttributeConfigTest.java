package com.todoapp.todorails.config;

import com.todoapp.todorails.model.User;
import com.todoapp.todorails.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalModelAttributeConfigTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void anonymousAuthenticationDoesNotFetchOrCreateAUser() {
        GlobalModelAttributeConfig config = new GlobalModelAttributeConfig(userRepository);
        var anonymous = new UsernamePasswordAuthenticationToken("anonymousUser", "",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));

        assertNull(config.addCurrentUser(anonymous));
        verify(userRepository, never()).findByUsername("anonymousUser");
    }

    @Test
    void authenticatedPrincipalResolvesToThePersistedUser() {
        GlobalModelAttributeConfig config = new GlobalModelAttributeConfig(userRepository);
        User persistedUser = new User("actual-user", "user@example.com", "encoded", "USER");
        var authentication = new UsernamePasswordAuthenticationToken("actual-user", "",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userRepository.findByUsername("actual-user")).thenReturn(Optional.of(persistedUser));

        assertSame(persistedUser, config.addCurrentUser(authentication));
    }
}
