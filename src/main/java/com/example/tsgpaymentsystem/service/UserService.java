package com.example.tsgpaymentsystem.service;

import com.example.tsgpaymentsystem.domain.Roles;
import com.example.tsgpaymentsystem.domain.User;
import com.example.tsgpaymentsystem.dto.UserDTO;
import com.example.tsgpaymentsystem.exception.UserAlreadyExistException;
import com.example.tsgpaymentsystem.exception.UserNotFoundException;
import com.example.tsgpaymentsystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    public User createUser(UserDTO dto) throws Exception {
        if (userRepository.findByEmail(dto.getEmail()).isPresent())
            throw new UserAlreadyExistException("User with the given email already exists" + dto.getEmail());

        User user = new User();
        user.setName(dto.getName());
        user.setLastname(dto.getLastname());
        user.setEmail(dto.getEmail());
        user.setRole((Roles.USER));
        user.setPhone(dto.getPhone());
        user.setIsAktive(true);
        user.setApi(true);
        user.setIsAggregated(true);
        user.setAddress(dto.getAddress());
        user.setContract(dto.getContract());
        user.setPassword(encoder.encode(dto.getPassword()));

        return userRepository.save(user);

    }

    public User updateUser(UserDTO dto) {

        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setIsAktive(dto.isActive());
        user.setApi(dto.isApi());
        user.setIsAggregated(dto.isAggregated());
        user.setAddress(dto.getAddress());
        user.setContract(dto.getContract());
        user.setPassword(encoder.encode(dto.getPassword()));
        user = userRepository.save(user);
        return user;
    }
    public User updateSettingsFields(User user, UserDTO dto) {
        user.setContract(dto.getContract());
        user = userRepository.save(user);
        return user;
    }
    public UserDTO getUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty())
            throw new UserNotFoundException("User cannot be found by ID " + userId);

        return UserDTO.toDto(user.get());
    }
}
