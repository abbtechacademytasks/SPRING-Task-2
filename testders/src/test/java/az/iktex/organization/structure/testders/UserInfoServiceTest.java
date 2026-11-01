package az.iktex.organization.structure.testders;

import az.iktex.organization.structure.testders.component.NotificationService;
import az.iktex.organization.structure.testders.component.RecentlyViewedUsers;
import az.iktex.organization.structure.testders.component.RequestContext;
import az.iktex.organization.structure.testders.component.UserAuditLog;
import az.iktex.organization.structure.testders.component.UserStatistics;
import az.iktex.organization.structure.testders.dto.request.CreateUserRequest;
import az.iktex.organization.structure.testders.dto.response.UserInfoResponse;
import az.iktex.organization.structure.testders.repository.UserRepository;
import az.iktex.organization.structure.testders.service.UserInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserInfoServiceTest {
    private UserRepository repository;
    private ObjectProvider<UserAuditLog> provider;
    private RecentlyViewedUsers recent;
    private UserInfoService service;
    private UserStatistics statistics;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        repository = mock(UserRepository.class);
        provider = mock(ObjectProvider.class);
        recent = mock(RecentlyViewedUsers.class);
        RequestContext context = mock(RequestContext.class);
        when(context.getRequestId()).thenReturn("test-id");
        statistics = new UserStatistics();
        service = new UserInfoService(repository, provider, context, recent, List.of(repository), statistics);
    }

    @Test
    void createWorksWithoutNotificationAndSetsAuditAction() {
        CreateUserRequest request = new CreateUserRequest("Ana", "ana@example.com");
        UserInfoResponse user = new UserInfoResponse();
        UserAuditLog audit = new UserAuditLog();
        when(provider.getObject()).thenReturn(audit);
        when(repository.createUser(request)).thenReturn(user);

        assertSame(user, service.createUser(request));
        assertEquals("CREATE_USER", audit.getAction());
        assertEquals(1L, statistics.getStatistics().get("createdUsers"));
        verify(repository).createUser(request);
    }

    @Test
    void sendsWelcomeToCreatedUser() {
        NotificationService notification = mock(NotificationService.class);
        service.setNotificationService(notification);
        CreateUserRequest request = new CreateUserRequest("Ana", "ana@example.com");
        UserInfoResponse user = new UserInfoResponse();
        user.setEmail(request.getEmail());
        when(repository.createUser(request)).thenReturn(user);
        when(provider.getObject()).thenReturn(new UserAuditLog());

        service.createUser(request);

        verify(notification).sendWelcome("ana@example.com");
    }

    @Test
    void missingUserIsNotAddedToHistory() {
        when(repository.getUserById(99L)).thenThrow(new NoSuchElementException());
        assertThrows(NoSuchElementException.class, () -> service.getUserById(99L));
        verifyNoInteractions(recent);
        assertEquals(1L, statistics.getStatistics().get("getUserByIdCalls"));
    }

    @Test
    void successfulLookupUpdatesHistory() {
        UserInfoResponse user = new UserInfoResponse();
        when(repository.getUserById(1L)).thenReturn(user);
        assertSame(user, service.getUserById(1L));
        verify(recent).addUser(1L);
    }

    @Test
    void recentReturnsFullUsersInSessionOrder() {
        UserInfoResponse first = new UserInfoResponse();
        first.setId(2L);
        UserInfoResponse second = new UserInfoResponse();
        second.setId(1L);
        when(recent.getUsers()).thenReturn(List.of(2L, 1L));
        when(repository.getUserById(2L)).thenReturn(first);
        when(repository.getUserById(1L)).thenReturn(second);
        assertEquals(List.of(first, second), service.getRecentUsers());
        verify(recent, never()).addUser(anyLong());
        assertEquals(0L, statistics.getStatistics().get("getUserByIdCalls"));
    }

    @Test
    void allUsersComeFromInjectedRepository() {
        List<UserInfoResponse> users = List.of(new UserInfoResponse());
        when(repository.getAllUsers()).thenReturn(users);
        assertSame(users, service.getAllUsers());
    }
}
