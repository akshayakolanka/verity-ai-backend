package com.verity.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfService {

    public List<PageText> extractPages(MultipartFile file) throws IOException {

        List<PageText> pages = new ArrayList<>();

        byte[] bytes = file.getBytes();

        try (PDDocument document = Loader.loadPDF(bytes)) {

            PDFTextStripper stripper = new PDFTextStripper();

            for (int i = 1; i <= document.getNumberOfPages(); i++) {

                stripper.setStartPage(i);
                stripper.setEndPage(i);

                String text = stripper.getText(document).trim();

                if (!text.isBlank()) {
                    pages.add(new PageText(i, text));
                }
            }
        }

        return pages;
    }

    public record PageText(int pageNumber, String text) {
    }
}