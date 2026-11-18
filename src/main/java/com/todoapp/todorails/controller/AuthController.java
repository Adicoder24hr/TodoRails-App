package com.todoapp.todorails.controller;

import com.todoapp.todorails.model.User;
import com.todoapp.todorails.service.DuplicateRegistrationException;
import com.todoapp.todorails.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String redirectToLogin(){
        return "redirect:/login";
    }

    //show registration form
    @GetMapping("/register")
    public String showRegisterForm(Model model){
        model.addAttribute("user",new User());
        return "register";
    }

    //Handle Form Submission
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            user.setPassword(null);
            model.addAttribute("errorMessage", "Please correct the highlighted registration details.");
            return "register";
        }

        try {
            userService.registerUser(user);
            return "redirect:/login";
        } catch (DuplicateRegistrationException exception) {
            String message = "username".equals(exception.getField())
                    ? "That username is already in use."
                    : "That email address is already registered.";
            return registrationError(user, model, message);
        } catch (DataIntegrityViolationException exception) {
            log.warn("Registration rejected by a database constraint");
            return registrationError(user, model,
                    "An account with those details may already exist. Please check the username and email.");
        } catch (Exception exception) {
            log.error("Unexpected registration failure ({})", exception.getClass().getSimpleName());
            return registrationError(user, model,
                    "We could not create your account right now. Please try again later.");
        }
    }

    private String registrationError(User user, Model model, String message) {
        user.setPassword(null);
        model.addAttribute("user", user);
        model.addAttribute("errorMessage", message);
        return "register";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }
}
