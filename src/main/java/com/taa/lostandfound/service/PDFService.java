package com.taa.lostandfound.service;

import com.taa.lostandfound.error.PDFExtractionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
public class PDFService {

    public String extractPDFContent(MultipartFile file) {
        log.info("Extracting content of PDF '{}'", file.getOriginalFilename());

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        } catch (IOException e) {
            throw new PDFExtractionException(
                    "Content of PDF file '%s' could not be extracted. ".formatted(file.getOriginalFilename()) +
                    "Please check the validity of the file"
            );
        }
    }
}
