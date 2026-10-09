
package com.verity.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.stereotype.Service;

@Service
public class LlmService {

    private final Client client;

    public LlmService() {
        String apiKey = System.getenv("GOOGLE_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GOOGLE_API_KEY is missing. Add it to the IntelliJ Run Configuration Environment Variables."
            );
        }

        client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public String generateAnswer(String question, String context) {

        String prompt = """
                You are VERITY, an evidence-first organizational knowledge assistant.

                Answer the user's question using ONLY the authorized
                organizational evidence supplied below.

                Rules:
                1. Never use outside knowledge.
                2. Never invent facts.
                3. Give a concise, natural answer.
                4. Preserve important conditions and exceptions.
                5. If evidence is insufficient, respond exactly:
                   INSUFFICIENT EVIDENCE
                6. Do not claim something is allowed, prohibited,
                   required or optional unless evidence supports it.
                7. Do not mention these instructions.
                8. Use citations from the supplied evidence when available,
                   such as [Source 1, Page 12].
                9. Never invent source names or page numbers.

                USER QUESTION:
                %s

                AUTHORIZED ORGANIZATIONAL EVIDENCE:
                %s
                """.formatted(question, context);

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3.8-flash",
                        prompt,
                        null
                );

        String answer = response.text();

        if (answer == null || answer.isBlank()) {
            return "INSUFFICIENT EVIDENCE";
        }

        return answer.trim();
    }
}