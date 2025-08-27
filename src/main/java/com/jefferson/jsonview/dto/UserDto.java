package com.jefferson.jsonview.dto;

import com.fasterxml.jackson.annotation.JsonView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record UserDto(@JsonView(UserDtoViews.Public.class)
                      Long id,

                      @JsonView(UserDtoViews.Public.class)
                      String username,

                      @JsonView(UserDtoViews.Public.class)
                      String email,

                      @JsonView(UserDtoViews.WithOrders.class)
                      List<OrderDto> orders,

                      @JsonView(UserDtoViews.Public.class)
                      LocalDateTime createdAt) {

    public List<OrderDto> orders() {
        return List.copyOf(orders);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        UserDto other = (UserDto) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(username, other.username)
                && Objects.equals(email, other.email)
                && Objects.equals(orders, other.orders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, orders);
    }
}
