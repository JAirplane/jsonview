package com.jefferson.jsonview.mapper;

import com.jefferson.jsonview.OrderStatus;
import com.jefferson.jsonview.dto.OrderDto;
import com.jefferson.jsonview.dto.UserDto;
import com.jefferson.jsonview.model.Order;
import com.jefferson.jsonview.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {UserMapperImpl.class, OrderMapperImpl.class})
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void toDtoWithOrders_ShouldMapAllFields() {

        User user = new User(1L, "test name", "test@example.com", false);
        Order order = new Order(1L, user, "test data", OrderStatus.CREATED);
        user.getOrders().add(order);

        OrderDto expectedOrderDto = new OrderDto(user.getId(), order.getOrderBucket());

        UserDto expected = new UserDto(1L, "test name",
                "test@example.com", List.of(expectedOrderDto));

        UserDto actual = userMapper.toDtoWithOrders(user);

        assertEquals(expected, actual);
    }

    @Test
    void toDtoWithoutOrders_OrdersShouldBeEmpty() {

        User user = new User(1L, "test name", "test@example.com", false);
        Order order = new Order(1L, user, "test data", OrderStatus.CREATED);
        user.getOrders().add(order);

        UserDto expected = new UserDto(1L, "test name", "test@example.com", null);

        UserDto actual = userMapper.toDtoWithoutOrders(user);

        assertEquals(expected, actual);
    }
}
