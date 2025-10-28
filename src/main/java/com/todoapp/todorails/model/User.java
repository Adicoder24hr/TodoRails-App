package com.todoapp.todorails.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    private String role = "USER";

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Todos> todos = new ArrayList<>();

    @Column(name = "profile_picture_path")
    private String profilePicturePath;

    public User(String username, String email, String password, String role){
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    //no - argument constructor

    public User(){

    }

    //Getters and Setters

    public Long getId(){
        return id;
    }

    public String getUsername(){
        return username;
    }

    public String getEmail(){
        return email;
    }

    public String getPassword(){
        return password;
    }

    public String getRole(){
        return role;
    }

    public void setId(Long id){
        this.id = id;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setPassword(String password){
        this.password = password;
    }
    public void setEmail(String email){
        this.email = email;
    }

    public void setRole(String role){
        this.role = role;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }
}
