package com.verity.controller;

import com.verity.dto.AskQuestionRequest;
import com.verity.dto.ChatResponse;
import com.verity.model.User;
import com.verity.service.LlmService;
import com.verity.service.RagService;
import com.verity.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final String COOKIE_NAME =
            "VERITY_SESSION";

    private final RagService ragService;
    private final UserService userService;
    private final LlmService llmService;

    public ChatController(
            RagService ragService,
            UserService userService,
            LlmService llmService) {

        this.ragService = ragService;
        this.userService = userService;
        this.llmService = llmService;
    }

    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(

            @CookieValue(
                    value = COOKIE_NAME,
                    required = false
            )
            String token,

            @RequestBody
            AskQuestionRequest request) {

        String question =
                request.getQuestion();

        /*
         * Validate question.
         */
        if (question == null ||
                question.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            new ChatResponse(
                                    "Please enter a question.",
                                    "INVALID_QUESTION",
                                    List.of()
                            )
                    );
        }

        /*
         * Authenticate user.
         */
        User user =
                userService.findByToken(token);

        if (user == null) {

            return ResponseEntity
                    .status(401)
                    .body(
                            new ChatResponse(
                                    "Please login first.",
                                    "UNAUTHORIZED",
                                    List.of()
                            )
                    );
        }

        /*
         * Get role from the database.
         */
        String userRole =
                user.getRole();

        if (userRole == null ||
                userRole.isBlank()) {

            userRole = "EMPLOYEE";
        }

        /*
         * RAG retrieval.
         */
        List<RagService.SearchResult> results =
                ragService.search(
                        question,
                        userRole
                );

        /*
         * No authorized evidence.
         */
        if (results.isEmpty()) {

            return ResponseEntity.ok(
                    new ChatResponse(
                            "I couldn't find sufficient authorized evidence in the organization's knowledge base to answer this question reliably.",
                            "INSUFFICIENT_EVIDENCE",
                            List.of()
                    )
            );
        }

        /*
         * Build evidence context.
         */
        StringBuilder context =
                new StringBuilder();

        for (
                RagService.SearchResult result
                : results
        ) {

            context
                    .append("SOURCE: ")
                    .append(
                            result.chunk()
                                    .getFileName()
                    )
                    .append("\n");

            context
                    .append("PAGE: ")
                    .append(
                            result.chunk()
                                    .getPageNumber()
                    )
                    .append("\n");

            context
                    .append("EVIDENCE:\n")
                    .append(
                            result.chunk()
                                    .getContent()
                    )
                    .append(
                            "\n\n--------------------\n\n"
                    );
        }

        /*
         * Generate answer.
         */
        String answer;

        try {

            answer =
                    llmService.generateAnswer(
                            question,
                            context.toString()
                    );

        } catch (Exception e) {

            e.printStackTrace();

            List<ChatResponse.SourceResponse>
                    sources =
                    results.stream()
                            .map(result ->
                                    new ChatResponse.SourceResponse(
                                            result.chunk()
                                                    .getFileName(),

                                            result.chunk()
                                                    .getPageNumber(),

                                            result.chunk()
                                                    .getContent(),

                                            result.score()
                                    )
                            )
                            .toList();

            return ResponseEntity.ok(
                    new ChatResponse(
                            "The relevant evidence was retrieved, but the OpenAI answer-generation service is currently unavailable.",
                            "LLM_ERROR",
                            sources
                    )
            );
        }

        /*
         * Source citations.
         */
        List<ChatResponse.SourceResponse>
                sources =
                results.stream()
                        .map(result ->
                                new ChatResponse.SourceResponse(
                                        result.chunk()
                                                .getFileName(),

                                        result.chunk()
                                                .getPageNumber(),

                                        result.chunk()
                                                .getContent(),

                                        result.score()
                                )
                        )
                        .toList();

        /*
         * Final grounded response.
         */
        return ResponseEntity.ok(
                new ChatResponse(
                        answer,
                        "GROUNDED",
                        sources
                )
        );
    }
}