package com.todoapp.todorails.controller;

import com.todoapp.todorails.model.PushSubscription;
import com.todoapp.todorails.model.User;
import com.todoapp.todorails.repository.PushSubscriptionRepository;
import com.todoapp.todorails.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class NotificationController {

    private final PushSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public NotificationController(PushSubscriptionRepository subscriptionRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/subscribe")
    public void subscribe(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestBody Map<String, Object> subscription) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        Map<String, String> keys = (Map<String, String>) subscription.get("keys");

        PushSubscription sub = new PushSubscription();
        sub.setEndpoint((String) subscription.get("endpoint"));
        sub.setP256dh(keys.get("p256dh"));
        sub.setAuth(keys.get("auth"));
        sub.setUser(user);

        subscriptionRepository.save(sub);
    }
}