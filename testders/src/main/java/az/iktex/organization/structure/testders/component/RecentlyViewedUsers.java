package az.iktex.organization.structure.testders.component;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@SessionScope
public class RecentlyViewedUsers {
    private final List<Long> users = new ArrayList<>();
    private final String sessionId = UUID.randomUUID().toString();

    public RecentlyViewedUsers() {
        System.out.println("RecentlyViewedUsers yaradildi: " + sessionId);
    }

    public synchronized void addUser(Long userId) {
        users.remove(userId);
        users.add(0, userId);
        if (users.size() > 5) {
            users.remove(5);
        }
    }

    public synchronized List<Long> getUsers() {
        return List.copyOf(users);
    }

    @PreDestroy
    public synchronized void destroy() {
        users.clear();
        System.out.println("RecentlyViewedUsers baglandi: " + sessionId);
    }
}
