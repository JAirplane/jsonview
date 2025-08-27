package com.jefferson.jsonview.service;

import com.jefferson.jsonview.dto.UserDto;
import com.jefferson.jsonview.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    Page<UserDto> getAllUsers(Pageable pageable);
    UserDto getUserById(Long userId);
    UserDto createNewUser(UserDto userDto);
    UserDto updateUserInfo(UserDto userDto);
    void deleteUser(Long userId);
}
