package az.iktex.organization.structure.testders.component;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
@ApplicationScope
public class UserStatistics {
    private final AtomicLong createdUsers = new AtomicLong();
    private final AtomicLong getUserByIdCalls = new AtomicLong();

    public void userCreated() {
        createdUsers.incrementAndGet();
    }

    public void userRequested() {
        getUserByIdCalls.incrementAndGet();
    }

    public Map<String, Long> getStatistics() {
        return Map.of("createdUsers", createdUsers.get(),
                "getUserByIdCalls", getUserByIdCalls.get());
    }
}
