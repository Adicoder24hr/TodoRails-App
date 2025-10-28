package com.todoapp.todorails.config;

import com.todoapp.todorails.model.User;
import com.todoapp.todorails.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {
    @Autowired
    private UserRepository userRepository;

    @ModelAttribute("user")
    public User addUserToModel(Authentication authentication){
        if(authentication == null || !authentication.isAuthenticated()){
            return null;
        }else{
            return userRepository.findByUsername(authentication.getName()).orElse(null);

        }
    }
}
