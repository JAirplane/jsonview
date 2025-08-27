package com.jefferson.jsonview.mapper;

import com.jefferson.jsonview.dto.UserDto;
import com.jefferson.jsonview.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OrderMapper.class)
public interface UserMapper {

    @Mapping(target = "orders", ignore = true)
    UserDto toDtoWithoutOrders(User user);

    UserDto toDtoWithOrders(User user);

}
