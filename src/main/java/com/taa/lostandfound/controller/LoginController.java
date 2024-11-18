package com.taa.lostandfound.controller;

import com.taa.lostandfound.error.MissingHeaderException;
import com.taa.lostandfound.model.RegistrationDTO;
import com.taa.lostandfound.model.RoleDTO;
import com.taa.lostandfound.model.UserDTO;
import com.taa.lostandfound.security.JwtUtil;
import com.taa.lostandfound.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
public class LoginController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public LoginController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping(value = "/authenticate", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> login(@RequestParam Map<String, String> body) {
        String userId = validateAndGet(body.get("username"), "Username is required.");
        String password = validateAndGet(body.get("password"), "Password is required.");
        UserDTO authenticatedUser = userService.authenticate(userId, password);
        HttpHeaders headers = getTokenHeader(authenticatedUser);
        return ResponseEntity.ok().headers(headers).body("Login successful");

    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<UserDTO> createUser(@RequestParam Map<String, String> body) {
        String userId = validateAndGet(body.get("email"), "Email is required.");
        String password = validateAndGet(body.get("password"), "Password is required.");
        String name = validateAndGet(body.get("name"), "Name is required.");
        RegistrationDTO registrationDTO = new RegistrationDTO(userId, password, name);
        UserDTO userDTO = userService.createUser(registrationDTO);
        HttpHeaders headers = getTokenHeader(userDTO);
        return ResponseEntity.ok().headers(headers).body(userDTO);
    }

    @GetMapping("/get-user")
    public ResponseEntity<UserDTO> getUser(@RequestHeader("Authorization") String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new MissingHeaderException("Authorization header is missing or invalid.");
        }
        UserDTO userDTO = userService.getUserFromToken(bearerToken);
        return ResponseEntity.ok(userDTO);
    }

    private HttpHeaders getTokenHeader(UserDTO userDTO) {
        HttpHeaders headers = new HttpHeaders();
        String token = jwtUtil.generateToken(
                userDTO.id(),
                userDTO.name(),
                userDTO.roles().stream().map(RoleDTO::name).toList()
        );
        headers.add("Authorization", "Bearer " + token);

        return headers;
    }

    private String validateAndGet(String value, String errorMessage) {
        return Optional.ofNullable(value)
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException(errorMessage));
    }
}
