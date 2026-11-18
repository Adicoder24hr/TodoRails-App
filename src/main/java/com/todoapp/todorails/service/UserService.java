package com.todoapp.todorails.service;

import com.todoapp.todorails.model.User;
import com.todoapp.todorails.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //Register new user
    public User registerUser(User user){
        Optional<User> existingUsername = userRepository.findByUsername(user.getUsername());
        if(existingUsername.isPresent()){
            throw new DuplicateRegistrationException("username");
        }

        Optional<User> existingEmail = userRepository.findByEmail(user.getEmail());
        if(existingEmail.isPresent()){
            throw new DuplicateRegistrationException("email");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }
}
