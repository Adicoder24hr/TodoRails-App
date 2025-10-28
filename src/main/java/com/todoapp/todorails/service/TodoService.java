package com.todoapp.todorails.service;

import com.todoapp.todorails.model.Todos;
import com.todoapp.todorails.model.User;
import com.todoapp.todorails.repository.TodoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository){
        this.todoRepository = todoRepository;
    }

    // Get all todos of a specific user
    public List<Todos> getTodosByUser(User user){
        return todoRepository.findByUser(user);
    }

    // Add new todo for a user
    public void addTodoForUser(User user, Todos todo){
        todo.setUser(user);
        todoRepository.save(todo);
    }

    // Mark Todo as completed and return a safe DTO
    public TodoDTO toggleCompleted(Long todoId){
        Todos todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + todoId));

        todo.setCompleted(!todo.isCompleted());
        Todos updatedTodo = todoRepository.save(todo);

        // Return only required fields as DTO
        return new TodoDTO(updatedTodo);
    }

    // Delete todo by id
    public boolean deleteTodoByIdAndUser(Long id, User user) {
        return todoRepository.findById(id)
                .filter(todo -> todo.getUser().equals(user))
                .map(todo -> {
                    todoRepository.delete(todo);
                    return true;
                })
                .orElse(false);
    }

    public List<Todos> getCompletedTodosByUser(User user){
        return todoRepository.findByUserAndCompletedTrue(user);
    }

    // DTO class for safe JSON response
    public static class TodoDTO {
        private Long id;
        private String title;
        private boolean completed;

        public TodoDTO(Todos todo){
            this.id = todo.getId();
            this.title = todo.getTitle();
            this.completed = todo.isCompleted();
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
}
