package com.taa.lostandfound.model;

public record LostItemDTO(
        Long id,
        String itemName,
        Integer quantity,
        String place
) {
    public LostItemDTO(String itemName, Integer quantity, String place) {
        this(null, itemName, quantity, place);
    }
}
