package com.jefferson.jsonview.service;

import com.jefferson.jsonview.OrderStatus;
import com.jefferson.jsonview.dto.OrderDto;
import com.jefferson.jsonview.dto.UserDto;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

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

        UserDto dto1 = new UserDto(1L, "John", "john@example.com", List.of());
        UserDto dto2 = new UserDto(2L, "Jane", "jane@example.com", List.of());

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

        UserDto userDto = new UserDto(1L, "John", "john@example.com", List.of(orderDto1, orderDto2));

        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDtoWithOrders(user)).thenReturn(userDto);

        UserDto actualDto = userService.getUserById(1L);

        assertEquals(userDto, actualDto);


        verify(userRepository).findByIdAndDeletedFalse(1L);
        verify(userMapper).toDtoWithOrders(user);
        verifyNoInteractions(orderRepository, orderMapper);
    }
}
