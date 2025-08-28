package com.jefferson.jsonview.mapper;

import com.jefferson.jsonview.OrderStatus;
import com.jefferson.jsonview.dto.OrderDto;
import com.jefferson.jsonview.model.Order;
import com.jefferson.jsonview.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = OrderMapperImpl.class)
public class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    void toDto_ShouldMapAllFields() {

        User user = new User(1L, "test name", "test@example.com", false);
        Order order = new Order(1L, user, "test data", OrderStatus.CREATED);
        user.getOrders().add(order);

        OrderDto expected = new OrderDto(1L, "test data");

        OrderDto actual = orderMapper.toDto(order);

        assertEquals(expected, actual);
    }

    @Test
    void toDto_shouldReturnNull() {

        OrderDto dto = orderMapper.toDto(null);

        assertNull(dto);
    }
}
