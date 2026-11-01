package az.iktex.organization.structure.testders.component;

import az.iktex.organization.structure.testders.service.UserInfoService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ScopeDemoController {
    private final UserInfoService userInfoService;
    private final ObjectProvider<UserAuditLog> auditProvider;
    private final RequestContext requestContext;
    private final UserStatistics userStatistics;

    public ScopeDemoController(UserInfoService userInfoService, ObjectProvider<UserAuditLog> auditProvider,
                               RequestContext requestContext, UserStatistics userStatistics) {
        this.userInfoService = userInfoService;
        this.auditProvider = auditProvider;
        this.requestContext = requestContext;
        this.userStatistics = userStatistics;
    }

    @GetMapping("/scope-demo")
    public Map<String, Object> scopeDemo() {
        return Map.of("singletonHashCode", userInfoService.hashCode(),
                "firstAuditId", auditProvider.getObject().getAuditId(),
                "secondAuditId", auditProvider.getObject().getAuditId(),
                "requestId", requestContext.getRequestId());
    }

    @GetMapping("/stats")
    public Map<String, Long> statistics() {
        return userStatistics.getStatistics();
    }
}
