package com.taa.lostandfound.model;

public record RoleDTO(
        Long id,
        String name
) {
    public RoleDTO(String name) {
        this(null, name);
    }
}
