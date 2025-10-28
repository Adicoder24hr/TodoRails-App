package com.todoapp.todorails.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenHash {
    public static void main(String[] args) {
        String raw = "Adiwar24hr@";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode(raw));
    }
}

