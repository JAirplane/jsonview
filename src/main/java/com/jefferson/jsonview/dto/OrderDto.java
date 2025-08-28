package com.jefferson.jsonview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

public record OrderDto(@NotNull(message = "Order dto: user id mustn't be null")
                       @Positive(message = "Order dto: user id must be positive")
                       Long userId,

                       @NotBlank(message = "Order dto: order bucket is null or empty")
                       String orderBucket) {

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        OrderDto other = (OrderDto) obj;
        return Objects.equals(userId, other.userId)
                && Objects.equals(orderBucket, other.orderBucket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, orderBucket);
    }
}
