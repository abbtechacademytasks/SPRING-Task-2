package az.iktex.organization.structure.testders.component;

import az.iktex.organization.structure.testders.dto.request.CreateUserRequest;
import az.iktex.organization.structure.testders.dto.response.UserInfoResponse;
import az.iktex.organization.structure.testders.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "app.repository-type", havingValue = "mock", matchIfMissing = true)
public class MockUserRepository implements UserRepository {
    private final Map<Long, UserInfoResponse> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();
    private final RequestContext requestContext;

    public MockUserRepository(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @PostConstruct
    public void init() {
        createUser(new CreateUserRequest("Mock User", "mock1@example.com"));
        createUser(new CreateUserRequest("Mock User", "mock2@example.com"));
    }

    @Override
    public UserInfoResponse getUserById(Long id) {
        System.out.println("[Repository] requestId=" + requestContext.getRequestId());
        UserInfoResponse user = users.get(id);
        if (user == null) {
            throw new NoSuchElementException("User tapilmadi: " + id);
        }
        return user;
    }

    @Override
    public List<UserInfoResponse> getAllUsers() {
        // Mock siyahı tapşırığa görə həmişə iki nümunə qaytarır.
        return List.of(users.get(1L), users.get(2L));
    }

    @Override
    public UserInfoResponse createUser(CreateUserRequest request) {
        UserInfoResponse user = new UserInfoResponse(idGenerator.incrementAndGet(),
                request.getFullName(), request.getEmail(), true, LocalDateTime.now());
        users.put(user.getId(), user);
        return user;
    }
}
