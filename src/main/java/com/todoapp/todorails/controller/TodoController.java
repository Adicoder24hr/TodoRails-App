package com.todoapp.todorails.controller;

import com.todoapp.todorails.TodoDTO;
import com.todoapp.todorails.model.Priority;
import com.todoapp.todorails.model.Todos;
import com.todoapp.todorails.model.User;
import com.todoapp.todorails.repository.TodoRepository;
import com.todoapp.todorails.repository.UserRepository;
import com.todoapp.todorails.service.TodoService;
import com.todoapp.todorails.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    // Dashboard: All active todos
    @GetMapping
    public String listTodos(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        // Fetch ALL todos (not just active)
        List<Todos> todos = todoRepository.findByUser(user);

        // Filter by priority
        List<Todos> highTodos = todoRepository.findByUserAndPriority(user, Priority.HIGH);
        List<Todos> mediumTodos = todoRepository.findByUserAndPriority(user, Priority.MEDIUM);
        List<Todos> lowTodos = todoRepository.findByUserAndPriority(user, Priority.LOW);

        model.addAttribute("user", user);
        model.addAttribute("todos", todos);
        model.addAttribute("highTodos", highTodos);
        model.addAttribute("mediumTodos", mediumTodos);
        model.addAttribute("lowTodos", lowTodos);
        model.addAttribute("currentPage", "DASHBOARD");

        return "dashboard";
    }

    // Add Todo
    @GetMapping("/add")
    public String showAddForm(Model model){
        model.addAttribute("newTodo", new Todos());
        model.addAttribute("currentPage", "ADD");
        return "add_todo";
    }

    @PostMapping("/add")
    public String addTodo(@AuthenticationPrincipal UserDetails userDetails, @ModelAttribute("newTodo") Todos todo){
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        todoService.addTodoForUser(user, todo);
        return "redirect:/todos";
    }

    // Toggle Completed / Active (normal POST redirect)
    @PostMapping("/toggle/{id}")
    public String toggleTodoStatus(@PathVariable Long id, @RequestParam(required = false) String from){
        Todos todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found!"));

        todo.setCompleted(!todo.isCompleted());
        todoRepository.save(todo);

        // Redirect logic based on context
        if (todo.isCompleted()) {
            return "redirect:/todos/history";
        } else if (from != null) {
            // Redirect to original priority page if toggle came from there
            switch (from.toUpperCase()) {
                case "HIGH": return "redirect:/todos/high";
                case "MEDIUM": return "redirect:/todos/medium";
                case "LOW": return "redirect:/todos/low";
                case "DASHBOARD": return "redirect:/todos";
                default: return "redirect:/todos";
            }
        } else {
            // Default: redirect to dashboard
            return "redirect:/todos";
        }
    }

    // Toggle Completed / Active (AJAX)
    @PostMapping("/toggle-ajax/{id}")
    @ResponseBody
    public ResponseEntity<TodoDTO> toggleTodoAjax(@PathVariable Long id){
        Todos todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found!"));

        // Toggle completed
        todo.setCompleted(!todo.isCompleted());
        todoRepository.save(todo);

        // Prepare DTO for frontend
        TodoDTO todoDTO = new TodoDTO(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.getPriority(),
                todo.isCompleted(),
                todo.getDueDate()
        );

        return ResponseEntity.ok(todoDTO);
    }

    // Delete Todo
    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteTodo(@PathVariable Long id, Principal principal){
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        boolean deleted = todoService.deleteTodoByIdAndUser(id, user);
        if(deleted){
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(404).body("Todo not found or unauthorized");
        }
    }

    // Edit Todo
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails){
        Todos todo = todoRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        if(!todo.getUser().getId().equals(user.getId())){
            return "redirect:/todos";
        }

        model.addAttribute("todo", todo);
        return "edit_todo";
    }

    @PostMapping("/edit/{id}")
    public String updateTodo(@PathVariable Long id, @ModelAttribute("todo") Todos updatedTodo,
                             @AuthenticationPrincipal UserDetails userDetails){
        Todos todo = todoRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        if(!todo.getUser().getId().equals(user.getId())){
            return "redirect:/todos";
        }

        todo.setTitle(updatedTodo.getTitle());
        todo.setDescription(updatedTodo.getDescription());
        todo.setPriority(updatedTodo.getPriority());
        todo.setDueDate(updatedTodo.getDueDate());

        todoRepository.save(todo);
        return "redirect:/todos";
    }

    // Priority pages
    @GetMapping("/high")
    public String highTodos(@AuthenticationPrincipal UserDetails userDetails, Model model){
        return getPriorityTodos(userDetails, model, Priority.HIGH, "HIGH");
    }

    @GetMapping("/medium")
    public String mediumTodos(@AuthenticationPrincipal UserDetails userDetails, Model model){
        return getPriorityTodos(userDetails, model, Priority.MEDIUM, "MEDIUM");
    }

    @GetMapping("/low")
    public String lowTodos(@AuthenticationPrincipal UserDetails userDetails, Model model){
        return getPriorityTodos(userDetails, model, Priority.LOW, "LOW");
    }

    private String getPriorityTodos(UserDetails userDetails, Model model, Priority priority, String pageName){
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        List<Todos> todos = todoRepository.findByUserAndPriorityAndCompletedFalse(user, priority);
        model.addAttribute("todos", todos);
        model.addAttribute("currentPage", pageName);
        return "priority-todos";
    }

    // Completed todos (history)
    @GetMapping("/history")
    public String completedTodos(@AuthenticationPrincipal UserDetails userDetails, Model model){
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        List<Todos> todos = todoRepository.findByUserAndCompletedTrue(user);
        model.addAttribute("todos", todos);
        model.addAttribute("currentPage", "HISTORY");
        return "history-todos";
    }

    // Update priority
    @PostMapping("/updatePriority/{id}")
    public String updatePriority(@PathVariable Long id, @RequestParam String priorityStr){
        Todos todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found!"));
        Priority priority;
        try{
            priority = Priority.valueOf(priorityStr.toUpperCase());
        } catch(IllegalArgumentException e){
            throw new RuntimeException("Invalid priority value: " + priorityStr);
        }

        todo.setPriority(priority);
        todoRepository.save(todo);

        switch(priority){
            case HIGH: return "redirect:/todos/high";
            case MEDIUM: return "redirect:/todos/medium";
            case LOW: return "redirect:/todos/low";
            default: return "redirect:/todos";
        }
    }
}
