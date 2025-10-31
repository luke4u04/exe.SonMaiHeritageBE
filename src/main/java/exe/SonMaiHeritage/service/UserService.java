package exe.SonMaiHeritage.service;

import exe.SonMaiHeritage.model.UserRegistrationRequest;
import exe.SonMaiHeritage.model.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    UserResponse registerUser(UserRegistrationRequest request);
    UserResponse getUserByUsername(String username);
    UserResponse getUserById(Integer id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Integer id, UserRegistrationRequest request);
    void deleteUser(Integer id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
