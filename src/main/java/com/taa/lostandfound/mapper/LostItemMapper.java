package com.taa.lostandfound.mapper;

import com.taa.lostandfound.entity.LostItemEntity;
import com.taa.lostandfound.model.LostItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LostItemMapper {
    @Mapping(target = "claims", ignore = true)
    LostItemEntity mapDtoToEntity(LostItemDTO lostItemDTO);
    LostItemDTO mapEntityToDto(LostItemEntity lostItemEntity);
}
