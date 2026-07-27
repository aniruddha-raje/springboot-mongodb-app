package com.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a post from the JSONPlaceholder API
 * (https://jsonplaceholder.typicode.com/posts).
 *
 * <p>{@code id} is {@code null} on create requests (the server assigns it) and
 * null fields are omitted when serialised, which keeps PATCH payloads partial.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Post(Long userId, Long id, String title, String body) {
}
