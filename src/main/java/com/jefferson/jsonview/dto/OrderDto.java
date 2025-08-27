package com.jefferson.jsonview.dto;

import com.jefferson.jsonview.OrderStatus;

import java.time.LocalDateTime;
import java.util.Objects;

public record OrderDto(Long id,
                       Long userId,
                       OrderStatus status,
                       LocalDateTime createdAt) {

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        OrderDto other = (OrderDto) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(userId, other.userId)
                && Objects.equals(this.status, other.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, status);
    }
}
