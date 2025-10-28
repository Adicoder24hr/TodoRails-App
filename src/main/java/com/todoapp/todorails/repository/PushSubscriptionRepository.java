package com.todoapp.todorails.repository;

import com.todoapp.todorails.model.PushSubscription;
import com.todoapp.todorails.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findByUser(User user);
}
