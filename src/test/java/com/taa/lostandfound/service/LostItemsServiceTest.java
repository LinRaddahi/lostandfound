package com.taa.lostandfound.service;

import com.taa.lostandfound.TestConstants;
import com.taa.lostandfound.entity.ClaimEntity;
import com.taa.lostandfound.entity.LostItemEntity;
import com.taa.lostandfound.entity.UserEntity;
import com.taa.lostandfound.error.NotFoundException;
import com.taa.lostandfound.mapper.ClaimMapper;
import com.taa.lostandfound.mapper.LostItemMapper;
import com.taa.lostandfound.mapper.UserMapper;
import com.taa.lostandfound.model.ClaimDTO;
import com.taa.lostandfound.model.LostItemDTO;
import com.taa.lostandfound.model.UserDTO;
import com.taa.lostandfound.repository.ClaimRepository;
import com.taa.lostandfound.repository.LostItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LostItemsServiceTest {

    private LostItemsService lostItemsService;
    private LostItemRepository lostItemRepository;
    private ClaimRepository claimRepository;
    private LostItemMapper lostItemMapper;
    private ClaimMapper claimMapper;
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        lostItemRepository = mock(LostItemRepository.class);
        claimRepository = mock(ClaimRepository.class);
        lostItemMapper = mock(LostItemMapper.class);
        claimMapper = mock(ClaimMapper.class);
        userMapper = mock(UserMapper.class);
        lostItemsService = new LostItemsService(
                lostItemRepository,
                claimRepository,
                lostItemMapper,
                claimMapper,
                userMapper
        );
    }

    @Test
    void createLostItems_withValidLostItems_shouldReturnSavedLostItems() {
        ArrayList<LostItemDTO> lostItems = new ArrayList<>();
        lostItems.add(new LostItemDTO(TestConstants.ITEM_NAME, 1, TestConstants.PLACE));
        LostItemEntity lostItemEntity = new LostItemEntity();
        when(lostItemMapper.mapDtoToEntity(any(LostItemDTO.class))).thenReturn(lostItemEntity);
        when(lostItemRepository.saveAll(anyList())).thenReturn(List.of(lostItemEntity));
        when(lostItemRepository.findById(anyLong())).thenReturn(null);

        when(lostItemMapper.mapEntityToDto(any(LostItemEntity.class)))
                .thenReturn(new LostItemDTO(TestConstants.ITEM_NAME, 1, TestConstants.PLACE));

        List<LostItemDTO> result = lostItemsService.createLostItems(lostItems);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TestConstants.ITEM_NAME, result.getFirst().itemName());
    }

    @Test
    void createLostItems_withExistingLostItems_shouldReturnEmptyList() {
        ArrayList<LostItemDTO> lostItems = new ArrayList<>();
        lostItems.add(new LostItemDTO(TestConstants.ITEM_NAME, 1, TestConstants.PLACE));
        when(lostItemRepository.findByItemNameAndPlace(anyString(), anyString())).thenReturn(new LostItemEntity());

        List<LostItemDTO> result = lostItemsService.createLostItems(lostItems);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getLostItems_withValidPageAndPerPage_shouldReturnLostItemsPage() {
        Page<LostItemEntity> lostItemEntities = new PageImpl<>(List.of(new LostItemEntity()));
        when(lostItemRepository.findAll(any(Pageable.class))).thenReturn(lostItemEntities);
        when(lostItemMapper.mapEntityToDto(any(LostItemEntity.class)))
                .thenReturn(new LostItemDTO(TestConstants.ITEM_NAME, 1, TestConstants.PLACE));

        Page<LostItemDTO> result = lostItemsService.getLostItems(1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(TestConstants.ITEM_NAME, result.getContent().getFirst().itemName());
    }

    @Test
    void claimItem_withValidUserAndItemId_shouldReturnClaimDTO() {
        UserDTO user = new UserDTO(TestConstants.USER_ID, TestConstants.NAME, null);
        LostItemEntity lostItemEntity = new LostItemEntity();
        ClaimEntity claimEntity = new ClaimEntity();
        when(lostItemRepository.findById(anyLong())).thenReturn(Optional.of(lostItemEntity));
        when(userMapper.mapDtoToEntity(any(UserDTO.class), isNull())).thenReturn(new UserEntity());
        when(claimRepository.save(any(ClaimEntity.class))).thenReturn(claimEntity);
        when(claimMapper.mapEntityToDto(any(ClaimEntity.class)))
                .thenReturn(new ClaimDTO(
                        new UserDTO(TestConstants.USER_ID, TestConstants.NAME, null),
                        new LostItemDTO("", 1, ""),
                        1)
                );
        ClaimDTO result = lostItemsService.claimItem(user, 1L, 1);

        assertNotNull(result);
    }

    @Test
    void claimItem_withInvalidItemId_shouldThrowNotFoundException() {
        UserDTO user = new UserDTO(TestConstants.USER_ID, TestConstants.NAME, null);
        Long itemId = 1L;
        int quantity = 1;
        when(lostItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> lostItemsService.claimItem(user, itemId, quantity));
    }

    @Test
    void getLostItemClaims_withValidPageAndPerPage_shouldReturnClaimsPage() {
        int page = 1;
        int perPage = 10;
        Page<ClaimEntity> claimEntities = new PageImpl<>(List.of(new ClaimEntity()));
        when(claimRepository.findAll(any(Pageable.class))).thenReturn(claimEntities);
        when(claimMapper.mapEntityToDto(any(ClaimEntity.class)))
                .thenReturn(new ClaimDTO(
                        new UserDTO(TestConstants.USER_ID, TestConstants.NAME, null),
                        new LostItemDTO("", 1, ""),
                        1)
                );

        Page<ClaimDTO> result = lostItemsService.getLostItemClaims(page, perPage);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}