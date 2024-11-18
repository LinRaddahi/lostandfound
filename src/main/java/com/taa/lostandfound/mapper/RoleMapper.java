package com.taa.lostandfound.mapper;

import com.taa.lostandfound.entity.RoleEntity;
import com.taa.lostandfound.model.RoleDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleEntity mapDtoToEntity(RoleDTO roleDTO);

    RoleDTO mapEntityToDto(RoleEntity roleEntity);
}
