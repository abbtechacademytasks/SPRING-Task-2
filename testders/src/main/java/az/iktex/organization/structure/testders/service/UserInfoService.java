package az.iktex.organization.structure.testders.service;

import az.iktex.organization.structure.testders.dto.request.CreateUserRequest;
import az.iktex.organization.structure.testders.dto.response.UserInfoResponse;
import az.iktex.organization.structure.testders.component.RequestContext;
import az.iktex.organization.structure.testders.component.RecentlyViewedUsers;
import az.iktex.organization.structure.testders.component.UserAuditLog;
import az.iktex.organization.structure.testders.component.NotificationService;
import az.iktex.organization.structure.testders.component.UserStatistics;
import az.iktex.organization.structure.testders.repository.UserRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInfoService {

    private final ObjectProvider<UserAuditLog> userAuditLog;
    private final UserRepository userRepository;
    private final RequestContext requestContext;
    private final RecentlyViewedUsers recentlyViewedUsers;
    private final List<UserRepository> repositories;
    private final UserStatistics userStatistics;
    private NotificationService notificationService;

    public UserInfoService(UserRepository userRepository,
                           ObjectProvider<UserAuditLog> userAuditLog,
                           RequestContext requestContext, RecentlyViewedUsers recentlyViewedUsers,
                           List<UserRepository> repositories, UserStatistics userStatistics) {
        this.userRepository = userRepository;
        this.userAuditLog = userAuditLog;
        this.requestContext = requestContext;
        this.recentlyViewedUsers = recentlyViewedUsers;
        this.repositories = List.copyOf(repositories);
        this.userStatistics = userStatistics;

        System.out.println("UserInfoService yaradildi: " + hashCode());
        System.out.println("Repository sayi: " + this.repositories.size());
    }

    @Autowired(required = false)
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public UserInfoResponse getUserById(Long id) {
        userStatistics.userRequested();
        System.out.println("[Service] requestId=" + requestContext.getRequestId());
        UserInfoResponse user = userRepository.getUserById(id);
        recentlyViewedUsers.addUser(id);
        return user;
    }

    public List<UserInfoResponse> getRecentUsers() {
        return recentlyViewedUsers.getUsers().stream()
                .map(userRepository::getUserById)
                .toList();
    }

    public List<UserInfoResponse> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public UserInfoResponse createUser(CreateUserRequest createUserRequest) {
        UserAuditLog audit = userAuditLog.getObject();
        audit.setAction("CREATE_USER");
        System.out.println("Audit: " + audit.getAuditId() + ", action=" + audit.getAction());
        UserInfoResponse user = userRepository.createUser(createUserRequest);
        userStatistics.userCreated();
        if (notificationService != null) {
            notificationService.sendWelcome(user.getEmail());
        }
        return user;
    }
}
