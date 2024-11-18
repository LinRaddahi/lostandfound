package com.taa.lostandfound.security;

import com.taa.lostandfound.SecurityConfigPropertiesTestConfig;
import com.taa.lostandfound.TestConstants;
import com.taa.lostandfound.error.JwtValidationException;
import com.taa.lostandfound.model.UserDTO;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Import(SecurityConfigPropertiesTestConfig.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(new SecurityConfigPropertiesTestConfig().securityConfigProperties());
    }

    @Test
    void generateToken_withValidInputs_shouldReturnToken() {
        String token = jwtUtil.generateToken(
                TestConstants.USER_ID,
                TestConstants.NAME,
                List.of(TestConstants.ROLE_USER)
        );

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(TestConstants.USER_ID);
        String token = jwtUtil.generateToken(
                TestConstants.USER_ID,
                TestConstants.NAME,
                List.of(TestConstants.ROLE_USER)
        );

        boolean isValid = jwtUtil.validateToken(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void validateToken_withInvalidToken_shouldThrowJwtValidationException() {
        String token = "invalidToken";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(TestConstants.USER_ID);

        JwtValidationException exception = assertThrows(JwtValidationException.class,
                () -> jwtUtil.validateToken(token, userDetails)
        );
        assertEquals("Error validating token", exception.getMessage());
    }

    @Test
    void getClaimsFromToken_withValidToken_shouldReturnClaims() {
        String token = jwtUtil.generateToken(
                TestConstants.USER_ID,
                TestConstants.NAME,
                List.of(TestConstants.ROLE_USER)
        );

        Claims claims = jwtUtil.getClaimsFromToken(token);

        assertNotNull(claims);
        assertEquals(TestConstants.USER_ID, claims.getSubject());
    }

    @Test
    void getClaimsFromToken_withInvalidToken_shouldThrowJwtValidationException() {
        String token = "invalidToken";

        JwtValidationException exception = assertThrows(JwtValidationException.class,
                () -> jwtUtil.getClaimsFromToken(token)
        );
        assertEquals("Error getting claims from token", exception.getMessage());
    }

    @Test
    void convert_withValidToken_shouldReturnUserDTO() {
        String token = jwtUtil.generateToken(
                TestConstants.USER_ID,
                TestConstants.NAME,
                List.of(TestConstants.ROLE_USER)
        );
        UserDTO userDTO = jwtUtil.convert(token);

        assertNotNull(userDTO);
        assertEquals(TestConstants.USER_ID, userDTO.id());
    }

    @Test
    void convert_withInvalidToken_shouldThrowJwtValidationException() {
        String token = "invalidToken";

        JwtValidationException exception = assertThrows(JwtValidationException.class, () -> jwtUtil.convert(token));
        assertEquals("Error converting token to user", exception.getMessage());
    }
}