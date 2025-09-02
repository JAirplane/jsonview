package com.jefferson.jsonview.service;

import com.jefferson.jsonview.OrderStatus;
import com.jefferson.jsonview.dto.OrderDto;
import com.jefferson.jsonview.dto.UserDto;
import com.jefferson.jsonview.exception.UserNotFoundException;
import com.jefferson.jsonview.mapper.OrderMapperImpl;
import com.jefferson.jsonview.mapper.UserMapperImpl;
import com.jefferson.jsonview.model.Order;
import com.jefferson.jsonview.model.User;
import com.jefferson.jsonview.repository.OrderRepository;
import com.jefferson.jsonview.repository.UserRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserMapperImpl userMapper;
    @Mock
    private OrderMapperImpl orderMapper;

    @BeforeEach
    public void initTests() {

        userService = new UserService(userRepository, userMapper, orderMapper);

        var validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.afterPropertiesSet();

        var validationInterceptor = new MethodValidationInterceptor(validatorFactory.getValidator());

        var proxyFactory = new ProxyFactory(userService);
        proxyFactory.addAdvice(validationInterceptor);

        userService = (UserService) proxyFactory.getProxy();
    }

    @Test
    void getAllUsers_ShouldReturnMappedPage() {

        Pageable pageable = PageRequest.of(0, 2);
        User user1 = new User(1L, "John", "john@example.com", false);
        User user2 = new User(2L, "Jane", "jane@example.com", false);

        Page<User> userPage = new PageImpl<>(List.of(user1, user2), pageable, 2);
        when(userRepository.findAllByDeletedFalse(pageable)).thenReturn(userPage);

        UserDto dto1 = new UserDto("John", "john@example.com", List.of());
        UserDto dto2 = new UserDto("Jane", "jane@example.com", List.of());

        when(userMapper.toDtoWithoutOrders(user1)).thenReturn(dto1);
        when(userMapper.toDtoWithoutOrders(user2)).thenReturn(dto2);

        Page<UserDto> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(List.of(dto1, dto2), result.getContent());

        verify(userRepository).findAllByDeletedFalse(pageable);
        verify(userMapper).toDtoWithoutOrders(user1);
        verify(userMapper).toDtoWithoutOrders(user2);
        verifyNoInteractions(orderRepository, orderMapper);
    }

    @Test
    public void getAllUsers_ShouldThrowConstraintViolationException() {

        assertThatThrownBy(() -> userService.getAllUsers(null))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("pageable") &&
                                            v.getMessage().equals("Pageable arg mustn't be null"));
                });
    }

    @Test
    void getUserById_ShouldReturnUserDto() {

        User user = new User(1L, "John", "john@example.com", false);
        Order order1 = new Order(1L, user, "test order bucket", OrderStatus.CREATED);
        Order order2 = new Order(1L, user, "test order bucket2", OrderStatus.CREATED);

        OrderDto orderDto1 = new OrderDto(1L, "test order bucket");
        OrderDto orderDto2 = new OrderDto(1L, "test order bucket2");

        user.getOrders().add(order1);
        user.getOrders().add(order2);

        UserDto userDto = new UserDto("John", "john@example.com", List.of(orderDto1, orderDto2));

        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDtoWithOrders(user)).thenReturn(userDto);

        UserDto actualDto = userService.getUserById(1L);

        assertEquals(userDto, actualDto);


        verify(userRepository).findByIdAndDeletedFalse(1L);
        verify(userMapper).toDtoWithOrders(user);
        verifyNoInteractions(orderRepository, orderMapper);
    }

    @Test
    public void getUserById_ShouldThrowConstraintViolationException_NullArg() {

        assertThatThrownBy(() -> userService.getUserById(null))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("userId") &&
                                            v.getMessage().equals("User id mustn't be null"));
                });
    }

    @Test
    public void getUserById_ShouldThrowConstraintViolationException_NotPositiveArg() {

        assertThatThrownBy(() -> userService.getUserById(0L))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("userId") &&
                                            v.getMessage().equals("User id must be positive"));
                });
    }

    @Test
    public void getUserById_ShouldThrowUserNotFoundException() {

        Long userId = 100L;
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(userId));

        assertEquals("User not found for id: " + userId, exception.getMessage());
        verify(userRepository).findByIdAndDeletedFalse(userId);
    }

    @Test
    public void createNewUser_NormalCase() {

        UserDto userDto = new UserDto("John", "john@example.com", null);

        User user = new User(1L, "John", "john@example.com", false);
        UserDto expected = new UserDto("John", "john@example.com", null);

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDtoWithoutOrders(user)).thenReturn(expected);

        UserDto actual = userService.createNewUser(userDto);

        assertEquals(expected, actual);
    }

    @Test
    public void createNewUser_ShouldThrowConstraintViolationException_NullArg() {

        assertThatThrownBy(() -> userService.createNewUser(null))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("userDto") &&
                                            v.getMessage().equals("User dto mustn't be null"));
                });
    }

    @Test
    public void createNewUser_ShouldThrowConstraintViolationException_NullDtoArgs() {

        UserDto userDto = new UserDto(null, null, null);

        assertThatThrownBy(() -> userService.createNewUser(userDto))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(2);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("username") &&
                                            v.getMessage().equals("User dto: username is null or empty"))
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("email") &&
                                            v.getMessage().equals("Email is null or empty"));
                });
    }

    @Test
    public void createNewUser_ShouldThrowConstraintViolationException_BadDtoArgs() {

        UserDto userDto = new UserDto("test name", "bad email", null);

        assertThatThrownBy(() -> userService.createNewUser(userDto))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("email") &&
                                            v.getMessage().equals("Invalid email format"));
                });
    }

    @Test
    public void updateUserInfo_NormalCase() {

        Long userId = 1L;

        UserDto userDtoArg = new UserDto("Changed", "changed@example.com", null);

        User user = new User(userId, "John", "john@example.com", false);
        User updatedUser = new User(userId, "Changed", "changed@example.com", false);
        UserDto expected = new UserDto("Changed", "changed@example.com", null);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(updatedUser);
        when(userMapper.toDtoWithoutOrders(user))
                .thenReturn(expected);

        UserDto actual = userService.updateUserInfo(userId, userDtoArg);

        assertEquals(expected, actual);
    }

    @Test
    public void updateUserInfo_ShouldThrowUserNotFoundException() {

        Long userId = 100L;
        UserDto userDto = new UserDto("test name", "test@email.com", List.of());

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.updateUserInfo(userId, userDto));

        assertEquals("User not found for id: " + userId, exception.getMessage());
        verify(userRepository).findByIdAndDeletedFalse(userId);
    }

    @Test
    public void updateUserInfo_ShouldThrowConstraintViolationException_NullArg() {

        assertThatThrownBy(() -> userService.updateUserInfo(null, null))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(2);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("userDto") &&
                                            v.getMessage().equals("User dto mustn't be null"))
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("userId") &&
                                            v.getMessage().equals("User id mustn't be null"));
                });
    }

    @Test
    public void updateUserInfo_ShouldThrowConstraintViolationException_NullDtoArgs() {

        UserDto userDto = new UserDto(null, null, null);

        assertThatThrownBy(() -> userService.updateUserInfo(0L, userDto))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(3);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("userId") &&
                                            v.getMessage().equals("User id must be positive"))
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("username") &&
                                            v.getMessage().equals("User dto: username is null or empty"))
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("email") &&
                                            v.getMessage().equals("Email is null or empty"));
                });
    }

    @Test
    public void updateUserInfo_ShouldThrowConstraintViolationException_BadDtoArgs() {

        UserDto userDto = new UserDto("test name", "bad email", null);

        assertThatThrownBy(() -> userService.updateUserInfo(1L, userDto))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("email") &&
                                            v.getMessage().equals("Invalid email format"));
                });
    }

    @Test
    public void addOrder_NormalCase() {

        Long userId = 1L;

        User user = new User(userId, "John", "john@example.com", false);
        OrderDto orderDtoArg = new OrderDto(userId, "Test bucket");
        Order order = new Order(userId, user, "Test bucket", OrderStatus.CREATED);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(orderMapper.toEntity(orderDtoArg)).thenReturn(order);

        userService.addOrder(orderDtoArg);

        verify(userRepository).findByIdAndDeletedFalse(userId);
        verify(orderMapper).toEntity(orderDtoArg);
        verify(userRepository).save(user);

    }

    @Test
    public void addOrder_ShouldThrowUserNotFoundException() {

        Long userId = 100L;
        OrderDto orderDto = new OrderDto(100L, "test bucket");

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.addOrder(orderDto));

        assertEquals("User not found for id: " + userId, exception.getMessage());
        verify(userRepository).findByIdAndDeletedFalse(userId);
    }

    @Test
    public void addOrder_ShouldThrowConstraintViolationException_NullArg() {

        assertThatThrownBy(() -> userService.addOrder(null))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("orderDto") &&
                                            v.getMessage().equals("Order dto mustn't be null"));
                });
    }

    @Test
    public void addOrder_ShouldThrowConstraintViolationException_NullDtoArgs() {

        OrderDto orderDto = new OrderDto(null, null);

        assertThatThrownBy(() -> userService.addOrder(orderDto))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(2);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("userId") &&
                                            v.getMessage().equals("Order dto: user id mustn't be null"))
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("orderBucket") &&
                                            v.getMessage().equals("Order dto: order bucket is null or empty"));
                });
    }

    @Test
    public void addOrder_ShouldThrowConstraintViolationException_BadDtoArgs() {

        OrderDto orderDto = new OrderDto(0L, null);

        assertThatThrownBy(() -> userService.addOrder(orderDto))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(2);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("userId") &&
                                            v.getMessage().equals("Order dto: user id must be positive"))
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("orderBucket") &&
                                            v.getMessage().equals("Order dto: order bucket is null or empty"));
                });
    }

    @Test
    public void deleteUser_NormalCase() {

        Long userId = 1L;

        User user = new User(userId, "John", "john@example.com", false);
        Order order1 = new Order(userId, user, "Test bucket", OrderStatus.CREATED);
        Order order2 = new Order(userId, user, "Test bucket2", OrderStatus.CREATED);
        user.getOrders().add(order1);
        user.getOrders().add(order2);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository).findByIdAndDeletedFalse(userId);
        verify(userRepository).save(user);
        assertThat(user.getDeleted()).isTrue();
        assertThat(order1.getStatus()).isEqualTo(OrderStatus.DELETED);
        assertThat(order2.getStatus()).isEqualTo(OrderStatus.DELETED);
    }

    @Test
    public void deleteUser_ShouldThrowConstraintViolationException_NullArg() {

        assertThatThrownBy(() -> userService.deleteUser(null))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("userId") &&
                                            v.getMessage().equals("User id mustn't be null"));
                });
    }

    @Test
    public void deleteUser_ShouldThrowConstraintViolationException_NotPositiveArg() {

        assertThatThrownBy(() -> userService.deleteUser(0L))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(exception -> {
                    var violations = ((ConstraintViolationException) exception).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations)
                            .anyMatch(v ->
                                    v.getPropertyPath().toString().contains("userId") &&
                                            v.getMessage().equals("User id must be positive"));
                });
    }
}
