package com.taa.lostandfound.controller;

import com.taa.lostandfound.TestConstants;
import com.taa.lostandfound.error.MissingHeaderException;
import com.taa.lostandfound.model.RegistrationDTO;
import com.taa.lostandfound.model.RoleDTO;
import com.taa.lostandfound.model.UserDTO;
import com.taa.lostandfound.security.JwtUtil;
import com.taa.lostandfound.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.util.Collection;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoginController.class)
@ExtendWith(SpringExtension.class)
@Import({TestcontainersConfiguration.class})
@AutoConfigureMockMvc
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetails userDetails;

    @BeforeEach
    public void setUp() {
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(TestConstants.ROLE_USER));
        doReturn(authorities).when(userDetails).getAuthorities();
        when(userService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtUtil.extractUserId(anyString())).thenReturn(TestConstants.USER_ID);
        when(jwtUtil.validateToken(anyString(), any(UserDetails.class))).thenReturn(true);
    }

    @Test
    public void loginWithValidCredentials() throws Exception {
        when(userService.authenticate(anyString(), anyString()))
                .thenReturn(new UserDTO(
                        TestConstants.USER_ID,
                        TestConstants.NAME,
                        List.of(new RoleDTO(TestConstants.ROLE_USER)))
                );
        when(jwtUtil.generateToken(anyString(), anyString(), anyList())).thenReturn("mockToken");

        mockMvc.perform(get("/authenticate")
                        .param("username", TestConstants.USER_ID)
                        .param("password", TestConstants.PASSWORD)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer mockToken"))
                .andExpect(content().string("Login successful"));
    }

    @Test
    public void loginWithMissingCredentials() throws Exception {
        mockMvc.perform(get("/authenticate")
                        .param("username", "")
                        .param("password", "")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username is required."));
    }

    @Test
    public void createUserWithValidData() throws Exception {
        UserDTO userDTO = new UserDTO(
                TestConstants.USER_ID,
                TestConstants.NAME,
                List.of(new RoleDTO(TestConstants.ROLE_USER))
        );
        when(userService.createUser(any(RegistrationDTO.class))).thenReturn(userDTO);
        when(jwtUtil.generateToken(anyString(), anyString(), anyList())).thenReturn("mockToken");

        mockMvc.perform(post("/register")
                        .param("email", TestConstants.USER_ID)
                        .param("password", TestConstants.PASSWORD)
                        .param("name", TestConstants.NAME)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer mockToken"))
                .andExpect(jsonPath("$.id").value(TestConstants.USER_ID))
                .andExpect(jsonPath("$.name").value(TestConstants.NAME));
    }

    @Test
    public void createUserWithMissingData() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "")
                        .param("password", "")
                        .param("name", "")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getUserWithValidToken() throws Exception {
        UserDTO userDTO = new UserDTO(
                TestConstants.USER_ID,
                TestConstants.NAME,
                List.of(new RoleDTO(TestConstants.ROLE_USER))
        );
        when(userService.getUserFromToken(anyString())).thenReturn(userDTO);

        mockMvc.perform(get("/get-user")
                        .header("Authorization", "Bearer validToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestConstants.USER_ID))
                .andExpect(jsonPath("$.name").value(TestConstants.NAME));
    }

    @Test
    public void getUserWithMissingToken() throws Exception {
        mockMvc.perform(get("/get-user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login.html"));
    }

    @Test
    public void getUserWithInvalidToken() throws Exception {
        when(userService.getUserFromToken(anyString()))
                .thenThrow(new MissingHeaderException("Authorization header is missing or invalid."));

        mockMvc.perform(get("/get-user")
                        .header("Authorization", "InvalidToken"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login.html"));
    }
}
