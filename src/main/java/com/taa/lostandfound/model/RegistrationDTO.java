package com.taa.lostandfound.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record RegistrationDTO(
    @Email
    @NotEmpty
    String email,

    @NotEmpty
    @Size(min = 8)
    String password,

    @NotEmpty
    String name
) {}
