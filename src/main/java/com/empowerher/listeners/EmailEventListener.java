package com.empowerher.listeners;

import com.empowerher.entities.User;
import com.empowerher.events.SchemeCreatedEvent;
import com.empowerher.events.UserRegisteredEvent;
import com.empowerher.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailEventListener {

    @Autowired
    private EmailService emailService;

    @EventListener
    @Async
    public void handleUserRegistered(UserRegisteredEvent event) {
        User user = event.getUser();
        System.out.println("Processing user registration event for: " + user.getEmail());
        
        // Send welcome email
        emailService.sendWelcomeEmail(user);
        
        // Send admin notification
        emailService.sendAdminNewUserNotification(user);
    }

    @EventListener
    @Async
    public void handleSchemeCreated(SchemeCreatedEvent event) {
        System.out.println("Processing scheme creation event for: " + event.getScheme().getTitle());
        
        // Send new scheme notifications
        emailService.sendNewSchemeNotification(event.getScheme());
    }
}