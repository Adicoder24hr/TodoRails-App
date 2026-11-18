package com.todoapp.todorails.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.todoapp.todorails.model.Todos;
import com.todoapp.todorails.repository.TodoRepository;

import jakarta.transaction.Transactional;
@Service
@Transactional
@ConditionalOnProperty(prefix = "app.reminders.email", name = "enabled", havingValue = "true")
public class EmailNotificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final TodoRepository todoRepository;
    private final JavaMailSender mailSender;
    private final String smtpUsername;
    private final String smtpPassword;

    public EmailNotificationService(TodoRepository todoRepository,
                                    JavaMailSender mailSender,
                                    @Value("${spring.mail.username:}") String smtpUsername,
                                    @Value("${spring.mail.password:}") String smtpPassword) {
        this.todoRepository = todoRepository;
        this.mailSender = mailSender;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
    }

    @Scheduled(fixedRate = 60000)
    public void checkTodosForEmailReminder() {
        if (smtpUsername.isBlank() || smtpPassword.isBlank()) {
            log.warn("Email reminders are enabled but SMTP credentials are not configured; skipping this run");
            return;
        }

        LocalDateTime dueBeforeUtc = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(1);
        List<Todos> todos = todoRepository
                .findByDueDateLessThanEqualAndCompletedFalseAndReminderSentFalse(dueBeforeUtc);

        int sentCount = 0;

        for (Todos todo : todos) {
            try {
                if (sendReminderEmail(todo)) {
                    todo.setReminderSent(true);
                    sentCount++;
                }
            } catch (RuntimeException exception) {
                log.warn("Email reminder delivery failed for todo id {} ({})",
                        todo.getId(), exception.getClass().getSimpleName());
            }
        }

        if (sentCount > 0) {
            todoRepository.saveAll(todos.stream().filter(Todos::isReminderSent).toList());
        }
        log.info("Email reminder run completed; sent {} reminder(s)", sentCount);
    }

    private boolean sendReminderEmail(Todos todo) {
        if (todo.getUser() == null || todo.getUser().getEmail() == null
                || todo.getUser().getEmail().isBlank()) {
            log.warn("Skipping email reminder for todo id {} because no recipient email is configured", todo.getId());
            return false;
        }

        String to = todo.getUser().getEmail();

        SimpleMailMessage message = new SimpleMailMessage();
        LocalDateTime isDueDate = todo.getDueDate()
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                .toLocalDateTime();
        message.setTo(to);
        message.setSubject("Todo Reminder: " + todo.getTitle());

        String body = "Hi " + todo.getUser().getUsername() + ",\n\n"
                + "This is a reminder for your todo:\n\n"
                + "Title: " + todo.getTitle() + "\n"
                + "Description: " + todo.getDescription() + "\n"
                + "Due Date: " + isDueDate + "\n\n"
                + "Please make sure to complete it on time.\n\n"
                + "Best regards,\n"
                + "TodoRails App";
        message.setText(body);
        mailSender.send(message);
        return true;
    }
}
