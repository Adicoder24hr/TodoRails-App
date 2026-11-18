package com.todoapp.todorails.service;

import com.todoapp.todorails.model.Todos;
import com.todoapp.todorails.model.User;
import com.todoapp.todorails.repository.TodoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceDueDateTest {

    @Mock
    private TodoRepository todoRepository;

    @Test
    void convertsNewSixPmIndiaDueDateToUtcBeforeSaving() {
        TodoService todoService = new TodoService(todoRepository);
        User user = new User();
        Todos todo = new Todos();
        todo.setDueDate(LocalDateTime.of(2026, 11, 17, 18, 0));

        todoService.addTodoForUser(user, todo);

        assertEquals(LocalDateTime.of(2026, 11, 17, 12, 30), todo.getDueDate());
        verify(todoRepository).save(todo);
    }

    @Test
    void deletesTodoWhenOwnerIsASeparateEntityInstanceWithSameId() {
        TodoService todoService = new TodoService(todoRepository);
        User authenticatedUser = new User();
        authenticatedUser.setId(7L);

        User separatelyLoadedOwner = new User();
        separatelyLoadedOwner.setId(7L);
        Todos todo = new Todos();
        todo.setId(41L);
        todo.setUser(separatelyLoadedOwner);
        when(todoRepository.findById(41L)).thenReturn(Optional.of(todo));

        assertTrue(todoService.deleteTodoByIdAndUser(41L, authenticatedUser));
        verify(todoRepository).delete(todo);
    }
}
