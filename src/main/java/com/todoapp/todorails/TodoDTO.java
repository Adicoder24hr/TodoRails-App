package com.todoapp.todorails;

import com.todoapp.todorails.model.Priority;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TodoDTO {
    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private boolean completed;
    private LocalDateTime dueDate;

    public TodoDTO(Long id, String title, String description, Priority priority, boolean completed, LocalDateTime dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.completed = completed;
        this.dueDate = dueDate;
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public boolean isCompleted() { return completed; }
    public LocalDateTime getDueDate() { return dueDate; }
}
