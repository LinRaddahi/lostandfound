package com.taa.lostandfound.service;

import com.taa.lostandfound.error.ParsingException;
import com.taa.lostandfound.model.LostItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ParserService {
    public ArrayList<LostItemDTO> parseLostItems(String extractedContent) {
        ArrayList<LostItemDTO> lostItems = new ArrayList<>();
        log.info("Parsing the content of the PDF");
        try {
            // Normalizing the content by removing extra newlines
            extractedContent = extractedContent.replaceAll("(?m)^[ \t]*\r?\n", "");

            String regex = "(?m)(LostItem:\\s*([^\\n]+))\\s*(Quantity:\\s*(\\d+))\\s*(Place:\\s*([^\\n]+))";
            Matcher matcher = Pattern.compile(regex, Pattern.DOTALL).matcher(extractedContent);

            while (matcher.find()) {
                lostItems.add(new LostItemDTO(
                        matcher.group(2),
                        Integer.parseInt(matcher.group(4)),
                        matcher.group(6))
                );
            }
        } catch (Exception e) {
            throw new ParsingException("Error occurred while parsing the content of the PDF", e);
        }
        return lostItems;
    }
}
