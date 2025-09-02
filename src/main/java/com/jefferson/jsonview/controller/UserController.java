package com.jefferson.jsonview.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.jefferson.jsonview.dto.OrderDto;
import com.jefferson.jsonview.dto.PageResponse;
import com.jefferson.jsonview.dto.UserDto;
import com.jefferson.jsonview.dto.UserDtoViews;
import com.jefferson.jsonview.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/users")
@Validated
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @JsonView(UserDtoViews.Public.class)
    public PageResponse<UserDto> allUsers(@PageableDefault(size = 10, page = 0)
                                  Pageable pageable) {
        Page<UserDto> page = userService.getAllUsers(pageable);
        return new PageResponse<>(page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements());
    }

    @GetMapping(path = "/{id}")
    @JsonView(UserDtoViews.WithOrders.class)
    public UserDto userById(@PathVariable
                            @Positive(message = "User id must be positive")
                            Long id) {
        return userService.getUserById(id);
    }

    @PostMapping(path = "/new")
    @JsonView(UserDtoViews.Public.class)
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto newUser(@RequestBody
                           @NotNull(message = "User dto mustn't be null")
                           @Valid
                           UserDto userDto) {
        return userService.createNewUser(userDto);
    }

    @PutMapping(path = "/update/{id}")
    @JsonView(UserDtoViews.Public.class)
    public UserDto updateUser(@PathVariable
                              @Positive(message = "User id must be positive")
                              Long id,
                              @RequestBody
                              @NotNull(message = "User dto mustn't be null")
                              @Valid
                              UserDto userDto) {
        return userService.updateUserInfo(id, userDto);
    }

    @PostMapping(path = "/order")
    public void addOrderToUser(@RequestBody
                               @NotNull(message = "Order dto mustn't be null")
                               @Valid
                               OrderDto orderDto) {
        userService.addOrder(orderDto);
    }

    @DeleteMapping(path = "/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable
                           @Positive(message = "User id must be positive")
                           Long id) {
        userService.deleteUser(id);
    }
}
