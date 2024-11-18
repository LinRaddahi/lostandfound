package com.taa.lostandfound;

import com.taa.lostandfound.error.InvalidPassException;
import com.taa.lostandfound.error.NotFoundException;
import com.taa.lostandfound.model.RegistrationDTO;
import com.taa.lostandfound.model.UserDTO;
import com.taa.lostandfound.repository.RoleRepository;
import com.taa.lostandfound.repository.UserRepository;
import com.taa.lostandfound.security.JwtUtil;
import com.taa.lostandfound.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(SecurityConfigPropertiesTestConfig.class)
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void authenticate_withValidCredentials_shouldReturnUserDTO() {
        userService.createUser(new RegistrationDTO(
                TestConstants.USER_ID,
                TestConstants.PASSWORD,
                TestConstants.NAME)
        );

        UserDTO authenticatedUser = userService.authenticate(TestConstants.USER_ID, TestConstants.PASSWORD);

        assertNotNull(authenticatedUser);
        assertEquals(TestConstants.USER_ID, authenticatedUser.id());
    }

    @Test
    void authenticate_withInvalidCredentials_shouldThrowInvalidPassException() {
        userService.createUser(new RegistrationDTO(
                TestConstants.USER_ID,
                TestConstants.PASSWORD,
                TestConstants.NAME)
        );

        assertThrows(InvalidPassException.class, () -> userService.authenticate(
                TestConstants.USER_ID,
                "wrongPassword")
        );
    }

    @Test
    void getUserFromToken_withValidToken_shouldReturnUserDTO() {
        userService.createUser(new RegistrationDTO(
                TestConstants.USER_ID,
                TestConstants.PASSWORD,
                TestConstants.NAME
        ));
        String token = jwtUtil.generateToken(
                TestConstants.USER_ID,
                TestConstants.NAME,
                List.of(TestConstants.ROLE_USER)
        );

        UserDTO userDTO = userService.getUserFromToken("Bearer " + token);

        assertNotNull(userDTO);
        assertEquals(TestConstants.USER_ID, userDTO.id());
    }

    @Test
    void getUserFromToken_withInvalidToken_shouldThrowException() {
        String token = "invalidToken";

        assertThrows(InvalidDataAccessApiUsageException.class, () ->
                userService.getUserFromToken("Bearer " + token)
        );
    }

    @Test
    void createUser_withValidData_shouldReturnUserDTO() {
        RegistrationDTO registrationDTO = new RegistrationDTO(
                TestConstants.USER_ID,
                TestConstants.PASSWORD,
                TestConstants.NAME
        );

        UserDTO userDTO = userService.createUser(registrationDTO);

        assertNotNull(userDTO);
        assertEquals(TestConstants.USER_ID, userDTO.id());
    }

    @Test
    void loadUserByUsername_withValidId_shouldReturnUserDetails() {
        userService.createUser(new RegistrationDTO(
                TestConstants.USER_ID,
                TestConstants.PASSWORD,
                TestConstants.NAME
        ));

        UserDetails userDetails = userService.loadUserByUsername(TestConstants.USER_ID);

        assertNotNull(userDetails);
        assertEquals(TestConstants.USER_ID, userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_withInvalidId_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> userService.loadUserByUsername("invalidId"));
    }
}