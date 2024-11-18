package com.taa.lostandfound.model;

public record ClaimDTO(
        Long id,
        UserDTO user,
        LostItemDTO lostItem,
        Integer quantity
) {
    public ClaimDTO(UserDTO userDTO, LostItemDTO lostItemDTO, Integer quantity) {
        this(null, userDTO, lostItemDTO, quantity);
    }
}
