package org.example.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.CreateUserRequestDto;
import org.example.userservice.dto.UpdateUserRequestDto;
import org.example.userservice.dto.UserResponseDto;
import org.example.userservice.service.UserService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Получить пользователя по ID", description = "Возвращает информацию о конкретном пользователе по его ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = { @Content(mediaType = "application/hal+json",
                            schema = @Schema(implementation = UserResponseDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Пользователь с таким ID не найден", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponseDto>> getUserById(
            @Parameter(description = "ID пользователя для поиска") @PathVariable Long id) {
        UserResponseDto userDto = userService.findUserById(id);
        addLinksToUser(userDto);
        return ResponseEntity.ok(EntityModel.of(userDto));
    }

    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех пользователей в системе")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UserResponseDto>>> getAllUsers() {
        List<UserResponseDto> userDtos = userService.findAllUsers();

        List<EntityModel<UserResponseDto>> userModels = userDtos.stream()
                .map(dto -> {
                    addLinksToUser(dto);
                    return EntityModel.of(dto);
                })
                .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel();
        return ResponseEntity.ok(CollectionModel.of(userModels, selfLink));
    }

    @Operation(summary = "Создать нового пользователя", description = "Создает нового пользователя и сохраняет его в базе данных")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные")
    })
    @PostMapping
    public ResponseEntity<EntityModel<UserResponseDto>> createUser(@RequestBody CreateUserRequestDto requestDto) {
        UserResponseDto createdUserDto = userService.createUser(requestDto);
        addLinksToUser(createdUserDto);

        return ResponseEntity
                .created(createdUserDto.getRequiredLink("self").toUri())
                .body(EntityModel.of(createdUserDto));
    }

    @Operation(summary = "Обновить пользователя", description = "Обновляет данные существующего пользователя по его ID")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponseDto>> updateUser(
            @Parameter(description = "ID пользователя для обновления") @PathVariable Long id,
            @RequestBody UpdateUserRequestDto requestDto) {
        UserResponseDto updatedUserDto = userService.updateUser(id, requestDto);
        addLinksToUser(updatedUserDto);
        return ResponseEntity.ok(EntityModel.of(updatedUserDto));
    }

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по его ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь с таким ID не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя для удаления") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    private void addLinksToUser(UserResponseDto userDto) {
        userDto.add(linkTo(methodOn(UserController.class).getUserById(userDto.getId())).withSelfRel());
        userDto.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));
    }
}