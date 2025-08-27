package com.jefferson.jsonview.mapper;

import com.jefferson.jsonview.dto.OrderDto;
import com.jefferson.jsonview.model.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderDto toDto(Order order);
}
