package com.jefferson.jsonview.dto;

import com.fasterxml.jackson.annotation.JsonView;

import java.util.List;

public record PageResponse<T>(
        @JsonView(UserDtoViews.Public.class) List<T> content,
        int page,
        int size,
        long totalElements
) {}
