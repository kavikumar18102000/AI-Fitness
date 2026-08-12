package com.fitness.userservie;

import com.fitness.dto.RegisterRequest;
import com.fitness.dto.UserResponse;
import com.fitness.model.User;
import com.fitness.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserResponse registerUser(RegisterRequest registerRequest) {

        if(userRepository.existsByEmail(registerRequest.getEmail())) {
//            throw new RuntimeException("Email already exists");
            User existingUser = userRepository.findByEmail(registerRequest.getEmail());
            return generateUserResponse(existingUser);
        }

        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setKeycloakId(registerRequest.getKeycloakId());

        User savedUser = userRepository.save(user);
        return generateUserResponse(savedUser);
    }

    public UserResponse getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return generateUserResponse(user);
    }

    public Boolean validateUser(String userId){
        log.info("Validating User with ID: {}", userId);
        boolean b = userRepository.existsByKeycloakId(userId);
        log.info("Validation Status for User: {}", b);
        return userRepository.existsByKeycloakId(userId);
    }

    private UserResponse generateUserResponse(User user) {
        UserResponse userResponse = new UserResponse();

        userResponse.setId(user.getId());
        userResponse.setKeycloakId(user.getKeycloakId());
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setPassword(user.getPassword());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());

        return userResponse;
    }
}
