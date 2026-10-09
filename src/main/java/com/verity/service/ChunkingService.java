package com.verity.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private static final int CHUNK_SIZE = 1200;
    private static final int OVERLAP = 200;

    public List<String> chunk(String text) {

        List<String> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        int start = 0;

        while (start < text.length()) {

            int end = Math.min(start + CHUNK_SIZE, text.length());

            String chunk = text.substring(start, end).trim();

            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            if (end == text.length()) {
                break;
            }

            start = end - OVERLAP;
        }

        return chunks;
    }
}