package az.iktex.organization.structure.testders.repository;

import az.iktex.organization.structure.testders.dto.request.CreateUserRequest;
import az.iktex.organization.structure.testders.dto.response.UserInfoResponse;

import java.util.List;

public interface UserRepository {
    UserInfoResponse getUserById(Long id);
    List<UserInfoResponse> getAllUsers();
    UserInfoResponse createUser(CreateUserRequest request);
}
