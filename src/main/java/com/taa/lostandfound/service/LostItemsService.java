package com.taa.lostandfound.service;

import com.taa.lostandfound.entity.ClaimEntity;
import com.taa.lostandfound.entity.LostItemEntity;
import com.taa.lostandfound.error.NotFoundException;
import com.taa.lostandfound.mapper.ClaimMapper;
import com.taa.lostandfound.mapper.LostItemMapper;
import com.taa.lostandfound.mapper.UserMapper;
import com.taa.lostandfound.model.ClaimDTO;
import com.taa.lostandfound.model.LostItemDTO;
import com.taa.lostandfound.model.UserDTO;
import com.taa.lostandfound.repository.ClaimRepository;
import com.taa.lostandfound.repository.LostItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LostItemsService {
    private static final Logger log = LoggerFactory.getLogger(LostItemsService.class);
    private final LostItemRepository lostItemRepository;
    private final ClaimRepository claimRepository;
    private final LostItemMapper lostItemMapper;
    private final ClaimMapper claimMapper;
    private final UserMapper userMapper;

    @Autowired
    public LostItemsService(
            LostItemRepository lostItemRepository,
            ClaimRepository claimRepository,
            LostItemMapper lostItemMapper,
            ClaimMapper claimMapper,
            UserMapper userMapper
    ) {
        this.lostItemRepository = lostItemRepository;
        this.claimRepository = claimRepository;
        this.lostItemMapper = lostItemMapper;
        this.claimMapper = claimMapper;
        this.userMapper = userMapper;
    }

    public List<LostItemDTO> createLostItems(ArrayList<LostItemDTO> lostItems) {
        log.info("Creating lost items");
        lostItems.removeIf(lostItem ->
                lostItemRepository.findByItemNameAndPlace(
                    lostItem.itemName(),
                    lostItem.place()
                ) != null
        );
        List<LostItemEntity> lostItemEntities = lostItems.stream().map(lostItemMapper::mapDtoToEntity).toList();
        return lostItemRepository.saveAll(lostItemEntities).stream().map(lostItemMapper::mapEntityToDto).toList();
    }

    public Page<LostItemDTO> getLostItems(int page, int perPage) {
        log.info("Getting all lost items on page {}, with {} items per page", page, perPage);
        Pageable pageable = PageRequest.of(Math.max((page-1), 0), perPage);
        return lostItemRepository.findAll(pageable).map(lostItemMapper::mapEntityToDto);
    }

    public ClaimDTO claimItem(UserDTO user, Long itemId, int quantity) {
        log.info("User {} is claiming item with id '{}' with quantity {}", user.id(), itemId, quantity);
        LostItemEntity lostItem =
                lostItemRepository.findById(itemId).orElseThrow(() ->
                        new NotFoundException(String.format("Lost item with id '%s' not found", itemId))

        );
        return claimMapper.mapEntityToDto(claimRepository.save(
                new ClaimEntity(userMapper.mapDtoToEntity(user, null), lostItem, quantity)
        ));

    }

    public Page<ClaimDTO> getLostItemClaims(int page, int perPage) {
        Pageable pageable = PageRequest.of(Math.max(page-1, 0), perPage);
        return claimRepository.findAll(pageable).map(claimMapper::mapEntityToDto);
    }
}
