package com.todoapp.todorails.controller;

import com.todoapp.todorails.model.Priority;
import com.todoapp.todorails.model.Todos;
import com.todoapp.todorails.model.User;
import com.todoapp.todorails.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoControllerDueDateTest {

    private static final Long TODO_ID = 41L;

    @Mock
    private TodoRepository todoRepository;

    private TodoController todoController;
    private User user;
    private Todos storedTodo;

    @BeforeEach
    void setUp() {
        todoController = new TodoController();
        ReflectionTestUtils.setField(todoController, "todoRepository", todoRepository);

        user = new User();
        user.setId(7L);
        user.setUsername("demo-user");

        storedTodo = new Todos();
        storedTodo.setId(TODO_ID);
        storedTodo.setTitle("Demo task");
        storedTodo.setPriority(Priority.MEDIUM);
        storedTodo.setUser(user);
        storedTodo.setDueDate(LocalDateTime.of(2026, 11, 17, 12, 30));
        storedTodo.setReminderSent(true);

        when(todoRepository.findById(TODO_ID)).thenReturn(Optional.of(storedTodo));
    }

    @Test
    void editRoundTripKeepsSixPmIstThenSupportsChangeAndClear() {
        ExtendedModelMap model = new ExtendedModelMap();
        model.addAttribute("user", user);

        assertEquals("edit_todo", todoController.showEditForm(TODO_ID, model));
        Todos editForm = (Todos) model.getAttribute("todo");
        LocalDateTime sixPmIst = LocalDateTime.of(2026, 11, 17, 18, 0);
        assertEquals(sixPmIst, editForm.getDueDate());

        todoController.updateTodo(TODO_ID, editForm, model);
        assertEquals(LocalDateTime.of(2026, 11, 17, 12, 30), storedTodo.getDueDate());
        org.junit.jupiter.api.Assertions.assertTrue(storedTodo.isReminderSent());

        editForm.setDueDate(LocalDateTime.of(2026, 11, 17, 20, 0));
        todoController.updateTodo(TODO_ID, editForm, model);
        assertEquals(LocalDateTime.of(2026, 11, 17, 14, 30), storedTodo.getDueDate());
        org.junit.jupiter.api.Assertions.assertFalse(storedTodo.isReminderSent());

        editForm.setDueDate(null);
        todoController.updateTodo(TODO_ID, editForm, model);
        assertNull(storedTodo.getDueDate());
    }
}
