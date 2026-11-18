package com.todoapp.todorails.controller;

import com.todoapp.todorails.config.GlobalModelAttributeConfig;
import com.todoapp.todorails.config.SecurityConfig;
import com.todoapp.todorails.model.User;
import com.todoapp.todorails.repository.UserRepository;
import com.todoapp.todorails.service.DuplicateRegistrationException;
import com.todoapp.todorails.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalModelAttributeConfig.class})
class AuthControllerRegistrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void registerPageRendersForAnonymousVisitorWithIndependentFormUser() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeDoesNotExist("currentUser"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("User Registration")));
    }

    @Test
    void loginPageRendersForAnonymousVisitor() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Login")));
    }

    @Test
    void successfulRegistrationRedirectsToLogin() throws Exception {
        when(userService.registerUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "new-demo-user")
                        .param("email", "demo@example.com")
                        .param("password", "long-enough-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"));
    }

    @Test
    void duplicateUsernameShowsFriendlyMessageInsteadOfExceptionDetails() throws Exception {
        when(userService.registerUser(any(User.class)))
                .thenThrow(new DuplicateRegistrationException("username"));

        mockMvc.perform(post("/register").with(csrf())
                        .param("username", "existing-user")
                        .param("email", "demo@example.com")
                        .param("password", "long-enough-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("errorMessage", "That username is already in use."))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Duplicate registration field"))));
    }

    @Test
    void invalidRegistrationRendersValidationErrorsWithoutCallingService() throws Exception {
        mockMvc.perform(post("/register").with(csrf())
                        .param("username", " ")
                        .param("email", "not-an-email")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("user", "username", "email", "password"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid email format")));
    }
}
