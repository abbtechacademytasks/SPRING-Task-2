package az.iktex.organization.structure.testders.service;

import az.iktex.organization.structure.testders.dto.request.CreateUserRequest;
import az.iktex.organization.structure.testders.dto.response.UserInfoResponse;
import az.iktex.organization.structure.testders.component.RequestContext;
import az.iktex.organization.structure.testders.component.UserAuditLog;
import az.iktex.organization.structure.testders.repository.UserRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInfoService {

    private final ObjectProvider<UserAuditLog> userAuditLog;
    private final UserRepository userRepository;
    private final RequestContext requestContext;

    public UserInfoService(UserRepository userRepository, ObjectProvider<UserAuditLog> userAuditLog, RequestContext requestContext) {
        this.userRepository = userRepository;
        this.userAuditLog = userAuditLog;
        this.requestContext = requestContext;

        System.out.println("UserInfoService yaradildi: " + hashCode());
    }

    public UserInfoResponse getUserById(Long id) {
        System.out.println("[Service] requestId=" + requestContext.getRequestId());
        return userRepository.getUserById(id);
    }

    public List<UserInfoResponse> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public UserInfoResponse createUser(CreateUserRequest createUserRequest) {
        UserAuditLog audit = userAuditLog.getObject();
        audit.setAction("CREATE_USER");
        System.out.println("Audit: " + audit.getAuditId() + ", action=" + audit.getAction());
        return userRepository.createUser(createUserRequest);
    }
}
