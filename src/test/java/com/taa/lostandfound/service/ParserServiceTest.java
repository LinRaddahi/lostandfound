package com.taa.lostandfound.service;

import com.taa.lostandfound.model.LostItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserServiceTest {

    private ParserService parserService;

    @BeforeEach
    void setUp() {
        parserService = new ParserService();
    }

    @Test
    void parseLostItems_withValidContent_shouldReturnLostItems() {
        String content = "LostItem: Wallet\nQuantity: 1\nPlace: Park\nLostItem: Phone\nQuantity: 2\nPlace: Office";

        ArrayList<LostItemDTO> result = parserService.parseLostItems(content);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Wallet", result.getFirst().itemName());
        assertEquals(1, result.getFirst().quantity());
        assertEquals("Park", result.getFirst().place());
        assertEquals("Phone", result.get(1).itemName());
        assertEquals(2, result.get(1).quantity());
        assertEquals("Office", result.get(1).place());
    }

    @Test
    void parseLostItems_withEmptyContent_shouldReturnEmptyList() {
        String content = "";

        ArrayList<LostItemDTO> result = parserService.parseLostItems(content);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseLostItems_withPartialValidContent_shouldReturnValidLostItems() {
        String content = "LostItem: Wallet\nQuantity: 1\nPlace: Park\nInvalid Content";

        ArrayList<LostItemDTO> result = parserService.parseLostItems(content);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Wallet", result.getFirst().itemName());
        assertEquals(1, result.getFirst().quantity());
        assertEquals("Park", result.getFirst().place());
    }
}