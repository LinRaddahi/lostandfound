package com.taa.lostandfound.mapper;

import com.taa.lostandfound.entity.UserEntity;
import com.taa.lostandfound.model.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {
    @Mapping(target = "claims", ignore = true)
    UserEntity mapDtoToEntity(UserDTO userDTO, String password);
    UserDTO mapEntityToDto(UserEntity userEntity);
}
