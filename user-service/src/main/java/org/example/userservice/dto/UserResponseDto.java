package org.example.userservice.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserResponseDto extends RepresentationModel<UserResponseDto> {
    private Long id;
    private String name;
    private String email;
    private int age;
    private LocalDateTime createdAt;
}