package com.jefferson.jsonview.dto;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Objects;

public record UserDto(@JsonView(UserDtoViews.Public.class)
                      @NotNull(message = "User dto: user id mustn't be null")
                      @Positive(message = "User dto: user id must be positive")
                      Long id,

                      @JsonView(UserDtoViews.Public.class)
                      @NotBlank(message = "User dto: username is null or empty")
                      String username,

                      @JsonView(UserDtoViews.Public.class)
                      @NotBlank(message = "Email is empty.")
                      @Email(message = "Invalid email format.")
                      String email,

                      @JsonView(UserDtoViews.WithOrders.class)
                      List<OrderDto> orders) {

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
