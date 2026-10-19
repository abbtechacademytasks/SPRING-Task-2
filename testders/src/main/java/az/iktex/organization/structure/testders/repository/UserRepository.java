package az.iktex.organization.structure.testders.repository;

import az.iktex.organization.structure.testders.dto.request.CreateUserRequest;
import az.iktex.organization.structure.testders.dto.response.UserInfoResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepository {
    private final Map<Long, UserInfoResponse> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    public UserRepository() {
        System.out.println("UserRepository yaradildi: " + hashCode());
    }

    @PostConstruct
    public void init() {
        createUser(new CreateUserRequest("Ana", "ana@example.com"));
        createUser(new CreateUserRequest("Murad", "murad@example.com"));
        createUser(new CreateUserRequest("Ali", "ali@example.com"));

        System.out.println("UserRepository hazirdir, 3 user yuklendi");
    }

    public UserInfoResponse getUserById(Long id) {
        UserInfoResponse user = users.get(id);

        if (user == null) {
            throw new NoSuchElementException("User tapilmadi: " + id);
        }

        return user;
    }

    public List<UserInfoResponse> getAllUsers() {
        return List.copyOf(users.values());
    }

    public UserInfoResponse createUser(CreateUserRequest createUserRequest) {
        UserInfoResponse user = new UserInfoResponse();
        user.setId(idGenerator.incrementAndGet());
        user.setFullName(createUserRequest.getFullName());
        user.setEmail(createUserRequest.getEmail());
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        users.put(user.getId(), user);

        return user;
    }

    @PreDestroy
    public void destroy() {
        users.clear();
        System.out.println("UserRepository baglandi, users temizlendi");
    }
}
