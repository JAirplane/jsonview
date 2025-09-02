package com.jefferson.jsonview.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jefferson.jsonview.dto.OrderDto;
import com.jefferson.jsonview.dto.UserDto;
import com.jefferson.jsonview.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    public void allUsers_ShouldReturnUsersWithoutOrders() throws Exception {

        Long userId2 = 2L;
        String username1 = "John";
        String username2 = "Jane";
        String email1 = "john@example.com";
        String email2 = "jane@example.com";

        OrderDto orderDto1 = new OrderDto(userId2, "test order bucket");
        OrderDto orderDto2 = new OrderDto(userId2, "test order bucket2");

        UserDto userDto1 = new UserDto(username1, email1, List.of());
        UserDto userDto2 = new UserDto(username2, email2, List.of(orderDto1, orderDto2));

        Pageable pageable = PageRequest.of(0, 2);

        Page<UserDto> userPage = new PageImpl<>(List.of(userDto1, userDto2), pageable, 2);

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].username").value(username1))
                .andExpect(jsonPath("$.content[0].email").value(email1))
                .andExpect(jsonPath("$.content[0].orders").doesNotExist())
                .andExpect(jsonPath("$.content[1].username").value(username2))
                .andExpect(jsonPath("$.content[1].email").value(email2))
                .andExpect(jsonPath("$.content[1].orders").doesNotExist());
    }

    @Test
    public void userById_ShouldReturnUserWithOrders() throws Exception {
        Long userId = 1L;
        String username = "John";
        String email = "john@example.com";

        OrderDto orderDto1 = new OrderDto(userId, "test order bucket");
        OrderDto orderDto2 = new OrderDto(userId, "test order bucket2");

        UserDto userDto = new UserDto(username, email, List.of(orderDto1, orderDto2));

        when(userService.getUserById(userId)).thenReturn(userDto);

        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.orders[0].userId").value(userId))
                .andExpect(jsonPath("$.orders[0].orderBucket").value("test order bucket"))
                .andExpect(jsonPath("$.orders[1].userId").value(userId))
                .andExpect(jsonPath("$.orders[1].orderBucket").value("test order bucket2"));
    }

    @Test
    public void newUser_ShouldReturnUserDtoWithoutOrders() throws Exception {

        String username = "John";
        String email = "john@example.com";

        UserDto userDto = new UserDto(username, email, List.of());

        when(userService.createNewUser(userDto)).thenReturn(userDto);

        mockMvc.perform(post("/api/v1/users/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.orders").doesNotExist());
    }

    @Test
    public void updateUser_ShouldReturnUserWithoutOrders() throws Exception {

        Long userId = 1L;
        String username = "Changed";
        String email = "changed@example.com";

        UserDto userDto = new UserDto(username, email, List.of());

        when(userService.updateUserInfo(userId, userDto)).thenReturn(userDto);

        mockMvc.perform(put("/api/v1/users/update/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.orders").doesNotExist());
    }

    @Test
    public void addOrderToUser_ShouldReturnIsOk() throws Exception {

        Long userId = 1L;
        String orderBucket = "test order bucket";

        OrderDto orderDto = new OrderDto(userId, orderBucket);

        mockMvc.perform(post("/api/v1/users/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk());
    }
}
