package com.taa.lostandfound.model;

import java.util.List;

public record UserDTO(
        String id,
        String name,
        List<RoleDTO> roles
) { }
