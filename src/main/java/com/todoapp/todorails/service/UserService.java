package com.todoapp.todorails.service;

import com.todoapp.todorails.model.Todos;
import com.todoapp.todorails.model.User;
import com.todoapp.todorails.repository.TodoRepository;
import com.todoapp.todorails.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TodoRepository todoRepository;

    //Register new user
    public User registerUser(User user){
        Optional<User> existingUsername = userRepository.findByUsername(user.getUsername());
        if(existingUsername.isPresent()){
            throw new RuntimeException("Username already taken");
        }

        Optional<User> existingEmail = userRepository.findByEmail(user.getEmail());
        if(existingEmail.isPresent()){
            throw new RuntimeException("Email already registered");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }
}
