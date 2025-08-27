package com.jefferson.jsonview.service;

import com.jefferson.jsonview.dto.UserDto;
import com.jefferson.jsonview.model.User;
import com.jefferson.jsonview.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Page<UserDto> getAllUsers(Pageable pageable) {

        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(user -> new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                List.of(),
                user.getCreatedAt()
        ));
    }

    @Override
    public UserDto getUserById(Long userId) {
        return null;
    }

    @Override
    public UserDto createNewUser(UserDto userDto) {
        return null;
    }

    @Override
    public UserDto updateUserInfo(UserDto userDto) {
        return null;
    }

    @Override
    public void deleteUser(Long userId) {

    }
}
