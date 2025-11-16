package org.example.userservice.service;

import lombok.RequiredArgsConstructor;

import org.example.userservice.dto.CreateUserRequestDto;
import org.example.userservice.dto.UpdateUserRequestDto;
import org.example.userservice.dto.UserResponseDto;
import org.example.userservice.entity.User;
import org.example.commondto.dto.event.EventType;
import org.example.commondto.dto.event.UserEvent;
import org.example.userservice.exception.ResourceNotFoundException;
import org.example.userservice.kafka.KafkaProducerService;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public UserResponseDto createUser(CreateUserRequestDto requestDto) {
        validate(requestDto.getName(), requestDto.getEmail(), requestDto.getAge());

        User user = userMapper.toUser(requestDto);
        User savedUser = userRepository.save(user);


        kafkaProducerService.sendUserEvent(new UserEvent(EventType.USER_CREATED, savedUser.getEmail()));


        return userMapper.toUserResponseDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + id + " not found."));
        return userMapper.toUserResponseDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UpdateUserRequestDto requestDto) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + id + " not found."));

        validate(requestDto.getName(), requestDto.getEmail(), requestDto.getAge());

        userToUpdate.setName(requestDto.getName());
        userToUpdate.setEmail(requestDto.getEmail());
        userToUpdate.setAge(requestDto.getAge());

        User updatedUser = userRepository.save(userToUpdate);
        return userMapper.toUserResponseDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + id + " not found."));

        String userEmail = userToDelete.getEmail();

        userRepository.deleteById(id);

        kafkaProducerService.sendUserEvent(new UserEvent(EventType.USER_DELETED, userEmail));
    }

    private void validate(String name, String email, int age) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (age <= 0) {
            throw new IllegalArgumentException("Age must be positive.");
        }
    }
}