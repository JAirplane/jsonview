package com.jefferson.jsonview.mapper;

import com.jefferson.jsonview.dto.OrderDto;
import com.jefferson.jsonview.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "user.id", target = "userId")
    OrderDto toDto(Order order);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    Order toEntity(OrderDto orderDto);
}

