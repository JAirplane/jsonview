package com.jefferson.jsonview.controller;

import com.jefferson.jsonview.dto.UserDto;
import com.jefferson.jsonview.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Page<UserDto> allUsers(@PageableDefault(size = 10, page = 0) Pageable pageable) {

        return userService.getAllUsers(pageable);
    }

    @GetMapping(path = "/{id}")
    public UserDto userById(@PathVariable Long id) {
        return null;
    }
}
