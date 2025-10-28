package com.todoapp.todorails.service;

import com.todoapp.todorails.model.PushSubscription;
import com.todoapp.todorails.model.Todos;
import com.todoapp.todorails.repository.PushSubscriptionRepository;
import com.todoapp.todorails.repository.TodoRepository;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final TodoRepository todoRepository;
    private final PushSubscriptionRepository subscriptionRepository;

    // Your VAPID keys
    private final String publicKey = "BATZoX_rWyC7k6hWcSwysM_OhmRCJgpuCZEAHO6JcHiQ9gCtg8PyU2b60P1YhLkZIiSaw5de0mnRifwCWLMUa6w";
    private final String privateKey = "aOmsNsduzisjUxNXSqYQuEML3KXQs9Wp2pP5N2J5HEk";

    static {
        // Add BouncyCastle provider once at startup
        Security.addProvider(new BouncyCastleProvider());
    }

    public NotificationService(TodoRepository todoRepository, PushSubscriptionRepository subscriptionRepository) {
        this.todoRepository = todoRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Scheduled(fixedRate = 60000) // runs every 1 minute
    public void checkTodosForNotification() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twoMinutesLater = now.plusMinutes(2);

            List<Todos> todos = todoRepository.findAllByDueDateBetweenAndCompletedFalse(now.minusSeconds(5), twoMinutesLater);

            for (Todos todo : todos) {
                List<PushSubscription> subs = subscriptionRepository.findByUser(todo.getUser());
                for (PushSubscription sub : subs) {
                    sendPushNotification(sub, todo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPushNotification(PushSubscription sub, Todos todo) {
        try {
            Subscription subscription = new Subscription(
                    sub.getEndpoint(),
                    new Subscription.Keys(sub.getP256dh(), sub.getAuth())
            );

            // Payload as a string (JSON)
            String payload = "{\"title\":\"Todo Reminder\",\"body\":\"" + todo.getTitle() + " is due!\"}";

            Notification notification = new Notification(subscription, payload);

            PushService pushService = new PushService()
                    .setPublicKey(publicKey)
                    .setPrivateKey(privateKey);

            pushService.send(notification);
            System.out.println("Notification sent for todo: " + todo.getTitle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
