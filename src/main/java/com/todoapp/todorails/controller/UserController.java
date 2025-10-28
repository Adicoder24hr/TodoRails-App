package com.todoapp.todorails.controller;

import com.todoapp.todorails.model.User;
import com.todoapp.todorails.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/settings")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB limit
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String settingsPage(Authentication authentication, Model model) {
        // ✅ FIX: redirect only if not authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        // ✅ FIX: Add timestamp for cache-busting in Thymeleaf
        model.addAttribute("timestamp", System.currentTimeMillis());
        model.addAttribute("currentPage", "SETTINGS");
        return "settings";
    }

    @PostMapping("/updateProfile")
    public String updateProfile(Authentication authentication,
                                @RequestParam("username") String username,
                                @RequestParam(value = "password", required = false) String password,
                                @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                                @RequestParam(value = "profilePicture", required = false) MultipartFile file,
                                Model model) throws IOException {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String currentUsername = authentication.getName();
        User user = userRepository.findByUsername(currentUsername).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        if (username != null && !username.isEmpty()) {
            user.setUsername(username);
        }
        // hash password before saving
        if (password != null && !password.isEmpty() && password.equals(confirmPassword)) {
            user.setPassword(passwordEncoder.encode(password));
        }

        // Handle profile picture upload or removal with error handling
        if (file != null && !file.isEmpty()) {
            // Validate file size (e.g., 2MB limit)
            if (file.getSize() > MAX_FILE_SIZE) {
                model.addAttribute("error", "Profile picture must be under 2MB.");
                model.addAttribute("user", user);
                return "settings";
            }
            // Validate file type (basic check for images)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                model.addAttribute("error", "Only image files are allowed.");
                model.addAttribute("user", user);
                return "settings";
            }

            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, file.getBytes());

            // Delete old profile picture if exists
            if (user.getProfilePicturePath() != null && !user.getProfilePicturePath().isEmpty()) {
                Path oldPath = Paths.get("src/main/resources/static" + user.getProfilePicturePath());
                Files.deleteIfExists(oldPath);
            }

            user.setProfilePicturePath("/uploads/" + fileName);
        } else {
            // Keep old profile picture if exists, only remove if user wants
            if (user.getProfilePicturePath() == null || user.getProfilePicturePath().isEmpty()) {
                user.setProfilePicturePath(null);
            }
        }

        userRepository.save(user);

        // ✅ FIX: Redirect to /settings to force a fresh page load and update the topbar instantly
        return "redirect:/settings";
    }
}