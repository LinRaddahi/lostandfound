package com.taa.lostandfound.mapper;

import com.taa.lostandfound.entity.ClaimEntity;
import com.taa.lostandfound.model.ClaimDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, LostItemMapper.class})
public interface ClaimMapper {
    ClaimDTO mapEntityToDto(ClaimEntity claimEntity);
}
