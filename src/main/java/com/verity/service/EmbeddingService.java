package com.verity.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingService {

    private static final int VECTOR_SIZE = 256;

    public List<Double> generateEmbedding(String text) {

        List<Double> vector = new ArrayList<>();

        for (int i = 0; i < VECTOR_SIZE; i++) {
            vector.add(0.0);
        }

        if (text == null || text.isBlank()) {
            return vector;
        }

        String[] words = text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .split("\\s+");

        for (String word : words) {

            if (word.isBlank()) {
                continue;
            }

            int index = Math.abs(word.hashCode()) % VECTOR_SIZE;

            vector.set(
                    index,
                    vector.get(index) + 1.0
            );
        }

        // Normalize the vector
        double magnitude = 0.0;

        for (double value : vector) {
            magnitude += value * value;
        }

        magnitude = Math.sqrt(magnitude);

        if (magnitude > 0) {

            for (int i = 0; i < vector.size(); i++) {
                vector.set(
                        i,
                        vector.get(i) / magnitude
                );
            }
        }

        return vector;
    }
}