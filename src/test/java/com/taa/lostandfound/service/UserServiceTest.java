package com.taa.lostandfound.service;

import com.taa.lostandfound.TestConstants;
import com.taa.lostandfound.entity.RoleEntity;
import com.taa.lostandfound.entity.UserEntity;
import com.taa.lostandfound.error.InvalidPassException;
import com.taa.lostandfound.error.NotFoundException;
import com.taa.lostandfound.mapper.UserMapper;
import com.taa.lostandfound.model.RegistrationDTO;
import com.taa.lostandfound.model.UserDTO;
import com.taa.lostandfound.repository.RoleRepository;
import com.taa.lostandfound.repository.UserRepository;
import com.taa.lostandfound.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserMapper userMapper;
    private JwtUtil jwtUtil;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        roleRepository = mock(RoleRepository.class);
        userRepository = mock(UserRepository.class);
        jwtUtil = mock(JwtUtil.class);
        userService = new UserService(userRepository, roleRepository, userMapper, jwtUtil);
    }

    @Test
    void loadUserByUsername_withValidId_shouldReturnUserDetails() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(TestConstants.USER_ID);
        userEntity.setPassword(TestConstants.PASSWORD);
        when(userRepository.findById(TestConstants.USER_ID)).thenReturn(Optional.of(userEntity));

        UserDetails userDetails = userService.loadUserByUsername(TestConstants.USER_ID);

        assertNotNull(userDetails);
        assertEquals(TestConstants.USER_ID, userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_withInvalidId_shouldThrowNotFoundException() {
        String userId = "invalidUserId";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                userService.loadUserByUsername(userId)
        );
        assertEquals(String.format("User with id '%s' not found", userId), exception.getMessage());
    }

    @Test
    void authenticate_withValidCredentials_shouldReturnUserDTO() {
        String password = TestConstants.PASSWORD;
        UserEntity userEntity = new UserEntity();
        userEntity.setId(TestConstants.USER_ID);
        userEntity.setPassword(new BCryptPasswordEncoder().encode(password));
        when(userRepository.findById(TestConstants.USER_ID)).thenReturn(Optional.of(userEntity));
        when(userMapper.mapEntityToDto(userEntity))
                .thenReturn(new UserDTO(TestConstants.USER_ID, TestConstants.NAME, null));

        UserDTO userDTO = userService.authenticate(TestConstants.USER_ID, password);

        assertNotNull(userDTO);
        assertEquals(TestConstants.USER_ID, userDTO.id());
    }

    @Test
    void authenticate_withInvalidPassword_shouldThrowInvalidPassException() {
        String password = "invalidPassword";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(TestConstants.USER_ID);
        userEntity.setPassword(new BCryptPasswordEncoder().encode(TestConstants.PASSWORD));
        when(userRepository.findById(TestConstants.USER_ID)).thenReturn(Optional.of(userEntity));

        InvalidPassException exception = assertThrows(InvalidPassException.class, () ->
                userService.authenticate(TestConstants.USER_ID, password)
        );
        assertEquals("Invalid password", exception.getMessage());
    }

    @Test
    void getUserFromToken_withValidToken_shouldReturnUserDTO() {
        String token = "Bearer validToken";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(TestConstants.USER_ID);
        when(jwtUtil.extractUserId(token.substring(7))).thenReturn(TestConstants.USER_ID);
        when(userRepository.findById(TestConstants.USER_ID)).thenReturn(Optional.of(userEntity));
        when(userMapper.mapEntityToDto(userEntity))
                .thenReturn(new UserDTO(TestConstants.USER_ID, TestConstants.NAME, null));

        UserDTO userDTO = userService.getUserFromToken(token);

        assertNotNull(userDTO);
        assertEquals(TestConstants.USER_ID, userDTO.id());
    }

    @Test
    void getUserFromToken_withInvalidToken_shouldThrowNotFoundException() {
        String token = "Bearer invalidToken";
        String userId = "invalidUserId";
        when(jwtUtil.extractUserId(token.substring(7))).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUserFromToken(token));
        assertEquals(String.format("User with id '%s' not found", userId), exception.getMessage());
    }

    @Test
    void createUser_withValidRegistrationDTO_shouldReturnUserDTO() {
        RegistrationDTO registrationDTO = new RegistrationDTO(
                TestConstants.USER_ID,
                TestConstants.PASSWORD,
                TestConstants.NAME
        );
        UserDTO userDTO = new UserDTO(TestConstants.USER_ID, TestConstants.NAME, null);
        UserEntity userEntity = new UserEntity();
        userEntity.setName(TestConstants.NAME);
        userEntity.setId(TestConstants.USER_ID);
        userEntity.setPassword(new BCryptPasswordEncoder().encode(TestConstants.PASSWORD));
        RoleEntity roleEntity = new RoleEntity(TestConstants.ROLE_USER);
        when(userMapper.mapDtoToEntity(any(), any())).thenReturn(userEntity);
        when(roleRepository.findByName(TestConstants.ROLE_USER)).thenReturn(Optional.of(roleEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.mapEntityToDto(userEntity)).thenReturn(userDTO);

        UserDTO createdUserDTO = userService.createUser(registrationDTO);

        assertNotNull(createdUserDTO);
        assertEquals(TestConstants.USER_ID, createdUserDTO.id());
    }
}