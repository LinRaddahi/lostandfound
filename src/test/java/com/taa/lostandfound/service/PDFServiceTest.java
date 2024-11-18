package com.taa.lostandfound.service;

import com.taa.lostandfound.error.PDFExtractionException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class PDFServiceTest {
    private final PDFService pdfService = new PDFService();

    @Test
    void extractPDFContent() throws Exception {
        byte[] pdfContent = Files.readAllBytes(Paths.get("src/test/resources/LostItems.pdf"));
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "LostItems.pdf",
                "application/pdf",
                pdfContent
        );
        String content = pdfService.extractPDFContent(mockFile);

        assertNotNull(content);
        assertFalse(content.isEmpty());
    }

    @Test
    void extractPDFContent_invalidFile() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "invalid.pdf",
                "application/pdf",
                new byte[0]
        );

        PDFExtractionException exception = assertThrows(PDFExtractionException.class, () ->
                pdfService.extractPDFContent(mockFile)
        );

        assertTrue(exception.getMessage().contains("Content of PDF file 'invalid.pdf' could not be extracted"));
    }
}