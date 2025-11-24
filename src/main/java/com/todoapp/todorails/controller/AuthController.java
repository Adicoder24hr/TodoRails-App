package com.todoapp.todorails.controller;

import com.todoapp.todorails.model.User;
import com.todoapp.todorails.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
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
    public String registerUser(@ModelAttribute("user") User user, Model model){
        try{
            userService.registerUser(user);
            return "redirect:/login";
        }catch(Exception e){
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", new User());
            return "register";
        }
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(){
        return "dashboard";
    }
}
