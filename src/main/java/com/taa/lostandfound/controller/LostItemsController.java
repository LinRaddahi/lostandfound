package com.taa.lostandfound.controller;

import com.taa.lostandfound.error.MissingHeaderException;
import com.taa.lostandfound.model.ClaimDTO;
import com.taa.lostandfound.model.LostItemDTO;
import com.taa.lostandfound.model.UserDTO;
import com.taa.lostandfound.security.JwtUtil;
import com.taa.lostandfound.service.LostItemsService;
import com.taa.lostandfound.service.PDFService;
import com.taa.lostandfound.service.ParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/lost-items")
public class LostItemsController {
    private final LostItemsService lostItemsService;
    private final PDFService pdfService;
    private final ParserService parserService;
    private final JwtUtil jwtUtil;

    @Autowired
    public LostItemsController(
            LostItemsService lostItemsService,
            PDFService pdfService,
            ParserService parserService,
            JwtUtil jwtUtil
    ) {
        this.lostItemsService = lostItemsService;
        this.pdfService = pdfService;
        this.parserService = parserService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<Page<LostItemDTO>> getLostItems(
            @RequestParam(required = false, defaultValue = "1") String page,
            @RequestParam(required = false, defaultValue = "10") String perPage
    ) {
        int pageInt = parseIntOrThrow(page, "Invalid page value");
        int perPageInt = parseIntOrThrow(perPage, "Invalid perPage value");
        Page<LostItemDTO> lostItems = lostItemsService.getLostItems(pageInt, perPageInt);
        return ResponseEntity.ok(lostItems);

    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LostItemDTO>> addLostItems(@RequestPart MultipartFile lostItemsFile) {
        String fileContent = pdfService.extractPDFContent(lostItemsFile);
        ArrayList<LostItemDTO> lostItems = parserService.parseLostItems(fileContent);
        List<LostItemDTO> savedLostItems = lostItemsService.createLostItems(lostItems);
        return ResponseEntity.ok(savedLostItems);
    }

    @GetMapping(path = "/claims")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ClaimDTO>> getLostItemClaims(
            @RequestParam(required = false, defaultValue = "1") String page,
            @RequestParam(required = false, defaultValue = "10") String perPage
    ) {
        int pageInt = parseIntOrThrow(page, "Invalid page value");
        int perPageInt = parseIntOrThrow(perPage, "Invalid perPage value");
        Page<ClaimDTO> lostItemClaims = lostItemsService.getLostItemClaims(pageInt, perPageInt);
        return ResponseEntity.ok(lostItemClaims);
    }

    @PostMapping(path = "/{itemId}/claim")
    public ResponseEntity<ClaimDTO> claimLostItem(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable String itemId,
            @RequestParam(required = false, defaultValue = "1") String quantity
    ) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new MissingHeaderException("Authorization header is missing or invalid.");
        }
        UserDTO userDTO = jwtUtil.convert(bearerToken.substring(7));
        int quantityInt = parseIntOrThrow(quantity, "Invalid quantity value");
        long itemIdLong = parseLongOrThrow(itemId, "Invalid itemId value");
        ClaimDTO claimDTO = lostItemsService.claimItem(userDTO, itemIdLong, quantityInt);
        return ResponseEntity.ok(claimDTO);

    }

    private int parseIntOrThrow(String value, String errorMessage) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private long parseLongOrThrow(String value, String errorMessage) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
