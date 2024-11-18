package com.taa.lostandfound.service;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

@Slf4j
@Service
@Qualifier("userService")
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserMapper userMapper,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
    }

    @Override
    public UserDetails loadUserByUsername(String id) {
        log.info("Loading user with id '{}'", id);

        return userRepository.findById(id)
                .map(this::mapToUserDetails)
                .orElseThrow(() -> new NotFoundException(String.format("User with id '%s' not found", id)));
    }

    public UserDTO authenticate(String id, String password) {
        log.info("Authenticating user with id '{}'", id);
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("User with id '%s' not found", id))
        );
        if (passwordEncoder.matches(password, userEntity.getPassword())) {
            return userMapper.mapEntityToDto(userEntity);
        }
        throw new InvalidPassException("Invalid password");
    }

    public UserDTO getUserFromToken(String bearerToken) {
        log.info("Extracting user from token '{}'", bearerToken);
        String userId = jwtUtil.extractUserId(bearerToken.substring(7));
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("User with id '%s' not found", userId))
        );
        return userMapper.mapEntityToDto(userEntity);
    }

    public UserDTO createUser(RegistrationDTO registrationDTO) {
        log.info("Creating user with email '{}'", registrationDTO.email());
        String hashedPassword = passwordEncoder.encode(registrationDTO.password());
        UserDTO userDTO = new UserDTO(registrationDTO.email(), registrationDTO.name(), null);
        UserEntity userEntity = userMapper.mapDtoToEntity(userDTO, hashedPassword);
        RoleEntity roleEntity = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new RoleEntity("ROLE_USER")));
        userEntity.setRoles(Set.of(roleEntity));
        return userMapper.mapEntityToDto(userRepository.save(userEntity));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(UserEntity userEntity) {
        return userEntity.getRoles().stream().map(role -> (GrantedAuthority) role::getName).toList();
    }

    private UserDetails mapToUserDetails(UserEntity userEntity) {
        return User.builder()
                .username(userEntity.getId())
                .password(userEntity.getPassword())
                .authorities(getAuthorities(userEntity))
                .build();
    }
}
