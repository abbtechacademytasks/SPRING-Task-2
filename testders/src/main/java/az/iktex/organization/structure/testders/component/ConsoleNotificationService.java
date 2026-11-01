package az.iktex.organization.structure.testders.component;

import org.springframework.stereotype.Service;

@Service
public class ConsoleNotificationService implements NotificationService {
    @Override
    public void sendWelcome(String email) {
        System.out.println("Xoş gəldin: " + email);
    }
}
