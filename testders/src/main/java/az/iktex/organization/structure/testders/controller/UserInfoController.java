package az.iktex.organization.structure.testders.controller;

import az.iktex.organization.structure.testders.dto.request.CreateUserRequest;
import az.iktex.organization.structure.testders.dto.response.UserInfoResponse;
import az.iktex.organization.structure.testders.component.RecentlyViewedUsers;
import az.iktex.organization.structure.testders.component.RequestContext;
import az.iktex.organization.structure.testders.service.UserInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-info")
public class UserInfoController {
    private final UserInfoService userInfoService;
    private final RecentlyViewedUsers recentlyViewedUsers;
    private final RequestContext requestContext;

    public UserInfoController(UserInfoService userInfoService,
                              RecentlyViewedUsers recentlyViewedUsers,
                              RequestContext requestContext) {
        this.userInfoService = userInfoService;
        this.recentlyViewedUsers = recentlyViewedUsers;
        this.requestContext = requestContext;
        System.out.println("UserInfoController yaradildi: " + hashCode());
    }

    @GetMapping("/{id}")
    public UserInfoResponse getUserById(@PathVariable Long id) {
        System.out.println("[Controller] requestId=" + requestContext.getRequestId());
        System.out.println("RequestContext proxy: " + requestContext.getClass().getName());
        recentlyViewedUsers.addUser(id);
        UserInfoResponse user = userInfoService.getUserById(id);
        System.out.println("Request duration: "
                + (System.currentTimeMillis() - requestContext.getStartTime()) + " ms");
        return user;
    }

    @GetMapping("/all")
    public List<UserInfoResponse> getAllUsers() {
        return userInfoService.getAllUsers();
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public UserInfoResponse createUser(@RequestBody CreateUserRequest createUserRequest) {
        return userInfoService.createUser(createUserRequest);
    }

    @GetMapping("/recent")
    public List<Long> getUserIdsRecent() {
        return recentlyViewedUsers.getUsers();
    }

}
