package com.taa.lostandfound;

import com.taa.lostandfound.security.JwtAuthorizationFilter;
import com.taa.lostandfound.security.JwtUtil;
import com.taa.lostandfound.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class JwtAuthorizationFilterIntegrationTest {
    @Autowired
    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserService userService;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_withValidToken_shouldSetAuthentication() throws ServletException, IOException {
        String token = "validToken";
        String userId = TestConstants.USER_ID;
        UserDetails userDetails = mock(UserDetails.class);

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        when(jwtUtil.extractUserId(token)).thenReturn(userId);
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);
        when(userService.loadUserByUsername(userId)).thenReturn(userDetails);

        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withInvalidToken_shouldNotSetAuthentication() throws ServletException, IOException {
        String token = "invalidToken";

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        when(jwtUtil.extractUserId(token)).thenReturn(null);

        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withoutToken_shouldNotSetAuthentication() throws ServletException, IOException {
        jwtAuthorizationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
