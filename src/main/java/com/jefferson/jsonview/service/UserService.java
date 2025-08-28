package com.jefferson.jsonview.service;

import com.jefferson.jsonview.OrderStatus;
import com.jefferson.jsonview.dto.OrderDto;
import com.jefferson.jsonview.dto.UserDto;
import com.jefferson.jsonview.exception.UserNotFoundException;
import com.jefferson.jsonview.mapper.OrderMapper;
import com.jefferson.jsonview.mapper.UserMapper;
import com.jefferson.jsonview.model.Order;
import com.jefferson.jsonview.model.User;
import com.jefferson.jsonview.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@Validated
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;

    @Autowired
    public UserService(UserRepository userRepository,
                           UserMapper userMapper, OrderMapper orderMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
    }

    public Page<UserDto> getAllUsers(@NotNull(message = "Pageable arg mustn't be null") Pageable pageable) {

        Page<User> userPage = userRepository.findAllByDeletedFalse(pageable);
        return userPage.map(userMapper::toDtoWithoutOrders);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(@NotNull(message = "User id mustn't be null")
                               @Positive(message = "User id must be positive") Long userId) {

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found for id: " + userId));
        return userMapper.toDtoWithOrders(user);
    }

    @Transactional
    public UserDto createNewUser(@NotNull(message = "User dto mustn't be null") UserDto userDto) {

        User newUser = new User();
        newUser.setUsername(userDto.username());
        newUser.setEmail(userDto.email());

        User savedUser = userRepository.save(newUser);

        return userMapper.toDtoWithoutOrders(savedUser);
    }

    @Transactional
    public UserDto updateUserInfo(UserDto userDto) {

        User user = userRepository.findById(userDto.id())
                .orElseThrow(() -> new UserNotFoundException("User not found for id: " + userDto.id()));
        user.setUsername(userDto.username());
        user.setEmail(userDto.email());

        User savedUser = userRepository.save(user);

        return userMapper.toDtoWithoutOrders(savedUser);
    }

    @Transactional
    public void addOrder(OrderDto orderDto) {

        User user = userRepository.findById(orderDto.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found for id: " + orderDto.userId()));

        Order order = orderMapper.toEntity(orderDto);
        order.setUser(user);
        user.getOrders().add(order);

        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isPresent()) {
            User user = userOpt.get();
            for(Order order: user.getOrders()) {
                order.setStatus(OrderStatus.DELETED);
            }
            user.setDeleted(true);
            userRepository.save(user);
        }
    }
}
