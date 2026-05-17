package com.coworking.user.service.impl;

import com.coworking.user.config.JwtService;
import com.coworking.user.dto.LoginRequest;
import com.coworking.user.dto.RegisterRequest;
import com.coworking.user.dto.UserDTO;
import com.coworking.user.model.Role;
import com.coworking.user.model.User;
import com.coworking.user.repository.UserRepository;
import com.coworking.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserDTO registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(Role.CUSTOMER)
                .build();

        return mapToDTO(userRepository.save(user));
    }

    @Override
    public UserDTO createUserByAdmin(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(request.getRole() == null ? Role.EMPLOYEE : request.getRole())
                .build();

        return mapToDTO(userRepository.save(user));
    }

    @Override
    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password.");
        }

        return jwtService.generateToken(user.getUsername(), user.getRole().name(), user.getId());
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found."));
    }

    @Override
    public Map<Long, String> getUsernamesByIds(List<Long> ids) {
        Map<Long, String> usernames = new LinkedHashMap<>();
        userRepository.findAllById(ids).forEach(user -> usernames.put(user.getId(), user.getUsername()));
        return usernames;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
