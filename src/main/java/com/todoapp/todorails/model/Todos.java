package com.todoapp.todorails.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "todos")
public class Todos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private boolean completed = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }

    public boolean isCompleted(){
        return completed;
    }
    public void setCompleted(boolean completed){
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }

    public LocalDateTime getDueDate(){ return dueDate; }
    public void setDueDate(LocalDateTime dueDate){ this.dueDate = dueDate; }

    public User getUser(){
        return user;
    }
    public void setUser(User user){
        this.user = user;
    }

    public Priority getPriority(){
        return priority;
    }

    public void setPriority(Priority priority){
        this.priority = priority;
    }
}
