package com.todoapp.todorails.repository;

import com.todoapp.todorails.model.Priority;
import com.todoapp.todorails.model.Todos;
import com.todoapp.todorails.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todos, Long> {

    // All todos for a user
    List<Todos> findByUser(User user);

    // Only active todos
    List<Todos> findByUserAndCompletedFalse(User user);

    // Only completed todos
    List<Todos> findByUserAndCompletedTrue(User user);

    // Active todos by priority
    List<Todos> findByUserAndPriorityAndCompletedFalse(User user, Priority priority);

    // All todos by priority
    List<Todos> findByUserAndPriority(User user, Priority priority);

    // Optional: Count helpers
    long countByUser(User user);
    long countByUserAndCompletedFalse(User user);
    long countByUserAndCompletedTrue(User user);
    long countByUserAndPriorityAndCompletedFalse(User user, Priority priority);
    long countByUserAndPriority(User user, Priority priority);

    // Find todos between dates (for notifications)
    List<Todos> findAllByDueDateBetweenAndCompletedFalse(LocalDateTime start, LocalDateTime end);
}
