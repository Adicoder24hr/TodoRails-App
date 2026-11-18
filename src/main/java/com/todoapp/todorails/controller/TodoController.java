package com.todoapp.todorails.controller;

import com.todoapp.todorails.TodoDTO;
import com.todoapp.todorails.model.Priority;
import com.todoapp.todorails.model.Todos;
import com.todoapp.todorails.model.User;
import com.todoapp.todorails.repository.TodoRepository;
import com.todoapp.todorails.service.TodoService;
import com.todoapp.todorails.service.UserService;
import com.todoapp.todorails.util.TodoDateTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserService userService;

    // Dashboard: All active todos
    @GetMapping
    public String listTodos(Model model) {
        User user = getAuthenticatedUser(model);

        // Fetch ALL todos (not just active)
        List<Todos> todos = todoRepository.findByUser(user);

        // Filter by priority
        List<Todos> highTodos = todoRepository.findByUserAndPriority(user, Priority.HIGH);
        List<Todos> mediumTodos = todoRepository.findByUserAndPriority(user, Priority.MEDIUM);
        List<Todos> lowTodos = todoRepository.findByUserAndPriority(user, Priority.LOW);

        model.addAttribute("currentUser", user);
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
    public String addTodo(@ModelAttribute("newTodo") Todos todo, Model model){
        User user = getAuthenticatedUser(model);
        todoService.addTodoForUser(user, todo);
        return "redirect:/todos";
    }

    // Toggle Completed / Active (normal POST redirect)
    @PostMapping("/toggle/{id}")
    public String toggleTodoStatus(@PathVariable Long id,
                                   @RequestParam(required = false) String from,
                                   Model model){
        Todos todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found!"));
        if (!belongsToUser(todo, getAuthenticatedUser(model))) {
            return "redirect:/todos";
        }

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
    public ResponseEntity<TodoDTO> toggleTodoAjax(@PathVariable Long id, Model model){
        Todos todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found!"));
        if (!belongsToUser(todo, getAuthenticatedUser(model))) {
            return ResponseEntity.notFound().build();
        }

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
    public ResponseEntity<?> deleteTodo(@PathVariable Long id, Model model){
        User user = getAuthenticatedUser(model);

        boolean deleted = todoService.deleteTodoByIdAndUser(id, user);
        if(deleted){
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(404).body("Todo not found or unauthorized");
        }
    }

    // Edit Todo
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model){
        Todos todo = todoRepository.findById(id).orElseThrow();
        User user = getAuthenticatedUser(model);

        if(!todo.getUser().getId().equals(user.getId())){
            return "redirect:/todos";
        }

        Todos editForm = new Todos();
        editForm.setId(todo.getId());
        editForm.setTitle(todo.getTitle());
        editForm.setDescription(todo.getDescription());
        editForm.setPriority(todo.getPriority());
        editForm.setDueDate(TodoDateTimes.utcToIst(todo.getDueDate()));
        model.addAttribute("todo", editForm);
        return "edit_todo";
    }

    @PostMapping("/edit/{id}")
    public String updateTodo(@PathVariable Long id, @ModelAttribute("todo") Todos updatedTodo,
                             Model model){
        Todos todo = todoRepository.findById(id).orElseThrow();
        User user = getAuthenticatedUser(model);

        if(!todo.getUser().getId().equals(user.getId())){
            return "redirect:/todos";
        }

        todo.setTitle(updatedTodo.getTitle());
        todo.setDescription(updatedTodo.getDescription());
        todo.setPriority(updatedTodo.getPriority());
        var updatedDueDateUtc = TodoDateTimes.istToUtc(updatedTodo.getDueDate());
        if (!Objects.equals(todo.getDueDate(), updatedDueDateUtc)) {
            todo.setDueDate(updatedDueDateUtc);
            todo.setReminderSent(false);
        }

        todoRepository.save(todo);
        return "redirect:/todos";
    }

    // Priority pages
    @GetMapping("/high")
    public String highTodos(Model model){
        return getPriorityTodos(getAuthenticatedUser(model), model, Priority.HIGH, "HIGH");
    }

    @GetMapping("/medium")
    public String mediumTodos(Model model){
        return getPriorityTodos(getAuthenticatedUser(model), model, Priority.MEDIUM, "MEDIUM");
    }

    @GetMapping("/low")
    public String lowTodos(Model model){
        return getPriorityTodos(getAuthenticatedUser(model), model, Priority.LOW, "LOW");
    }

    private String getPriorityTodos(User user, Model model, Priority priority, String pageName){
        List<Todos> todos = todoRepository.findByUserAndPriorityAndCompletedFalse(user, priority);
        model.addAttribute("todos", todos);
        model.addAttribute("currentPage", pageName);
        return "priority-todos";
    }

    // Completed todos (history)
    @GetMapping("/history")
    public String completedTodos(Model model){
        User user = getAuthenticatedUser(model);
        List<Todos> todos = todoRepository.findByUserAndCompletedTrue(user);
        model.addAttribute("todos", todos);
        model.addAttribute("currentPage", "HISTORY");
        return "history-todos";
    }

    private User getAuthenticatedUser(Model model) {
        Object user = model.asMap().get("currentUser");
        if (user instanceof User authenticatedUser && authenticatedUser.getId() != null) {
            return authenticatedUser;
        }
        throw new UsernameNotFoundException("Authenticated user not found");
    }

    private boolean belongsToUser(Todos todo, User user) {
        return todo.getUser() != null && Objects.equals(todo.getUser().getId(), user.getId());
    }

    // Update priority
    @PostMapping("/updatePriority/{id}")
    public String updatePriority(@PathVariable Long id, @RequestParam String priorityStr, Model model){
        Todos todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found!"));
        if (!belongsToUser(todo, getAuthenticatedUser(model))) {
            return "redirect:/todos";
        }
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
