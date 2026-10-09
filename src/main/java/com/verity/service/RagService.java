package com.verity.service;

import com.verity.model.DocumentChunk;
import com.verity.repository.DocumentChunkRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class RagService {

    private final DocumentChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    private static final int MAX_RESULTS = 5;

    private static final double MIN_SCORE = 0.12;


    public RagService(
            DocumentChunkRepository chunkRepository,
            EmbeddingService embeddingService) {

        this.chunkRepository =
                chunkRepository;

        this.embeddingService =
                embeddingService;
    }


    public List<SearchResult> search(
            String question,
            String userRole) {

        if (question == null
                || question.isBlank()) {

            return List.of();
        }


        String normalizedQuestion =
                normalize(question);


        if (normalizedQuestion.isBlank()) {
            return List.of();
        }


        Set<String> keywords =
                extractKeywords(
                        normalizedQuestion
                );


        List<Double> queryEmbedding =
                embeddingService.generateEmbedding(
                        normalizedQuestion
                );


        List<DocumentChunk> chunks =
                chunkRepository.findAll();


        if (chunks == null
                || chunks.isEmpty()) {

            return List.of();
        }


        List<SearchResult> results =
                new ArrayList<>();


        for (DocumentChunk chunk :
                chunks) {

            if (chunk == null) {
                continue;
            }


            /*
             * SECURITY CHECK
             */
            if (!isAuthorized(
                    chunk,
                    userRole
            )) {
                continue;
            }


            String content =
                    chunk.getContent();


            if (content == null
                    || content.isBlank()) {

                continue;
            }


            String normalizedContent =
                    normalize(content);


            double keywordScore =
                    calculateKeywordScore(
                            keywords,
                            normalizedContent
                    );


            double phraseScore =
                    calculatePhraseScore(
                            normalizedQuestion,
                            normalizedContent
                    );


            double conceptScore =
                    calculateConceptScore(
                            keywords,
                            normalizedContent
                    );


            double vectorScore =
                    calculateVectorSimilarity(
                            queryEmbedding,
                            chunk.getEmbedding()
                    );


            double finalScore =
                    (keywordScore * 0.40)
                            + (phraseScore * 0.20)
                            + (conceptScore * 0.30)
                            + (vectorScore * 0.10);


            if (hasEvidence(
                    keywordScore,
                    phraseScore,
                    conceptScore,
                    vectorScore,
                    finalScore
            )) {

                results.add(
                        new SearchResult(
                                chunk,
                                finalScore,
                                keywordScore,
                                phraseScore,
                                conceptScore,
                                vectorScore
                        )
                );
            }
        }


        results.sort(
                Comparator
                        .comparingDouble(
                                SearchResult::score
                        )
                        .reversed()
        );


        List<SearchResult>
                uniqueResults =
                new ArrayList<>();


        Set<String> seen =
                new HashSet<>();


        for (
                SearchResult result
                : results
        ) {

            DocumentChunk chunk =
                    result.chunk();


            String key =
                    safe(
                            chunk.getDocumentId()
                    )
                            + "|"
                            + chunk.getPageNumber()
                            + "|"
                            + safe(
                            chunk.getContent()
                    );


            if (seen.add(key)) {

                uniqueResults.add(
                        result
                );
            }


            if (
                    uniqueResults.size()
                            >= MAX_RESULTS
            ) {
                break;
            }
        }


        if (uniqueResults.isEmpty()) {
            return List.of();
        }


        SearchResult strongest =
                uniqueResults.get(0);


        if (!isStrongEnough(
                strongest
        )) {

            return List.of();
        }


        return uniqueResults;
    }


    /* ============================================================
       ROLE AUTHORIZATION
       ============================================================ */

    private boolean isAuthorized(
            DocumentChunk chunk,
            String userRole) {

        String requiredRole =
                chunk.getRequiredRole();


        /*
         * Public document.
         */
        if (requiredRole == null
                || requiredRole.isBlank()
                || requiredRole.equalsIgnoreCase(
                "ALL"
        )
                || requiredRole.equalsIgnoreCase(
                "PUBLIC"
        )) {

            return true;
        }


        if (userRole == null
                || userRole.isBlank()) {

            return false;
        }


        String role =
                userRole.toUpperCase();


        String required =
                requiredRole.toUpperCase();


        /*
         * ADMIN
         *
         * Can access everything.
         */
        if (role.equals("ADMIN")) {
            return true;
        }


        /*
         * MANAGER
         *
         * Can access:
         * - MANAGER
         * - EMPLOYEE
         * - ALL
         */
        if (role.equals("MANAGER")) {

            return required.equals("MANAGER")
                    || required.equals("EMPLOYEE");
        }


        /*
         * EMPLOYEE
         *
         * Can access employee documents only.
         */
        if (role.equals("EMPLOYEE")) {

            return required.equals("EMPLOYEE");
        }


        return false;
    }


    /* ============================================================
       NORMALIZATION
       ============================================================ */

    private String normalize(
            String text) {

        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .replaceAll(
                        "[^a-z0-9\\s]",
                        " "
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }


    /* ============================================================
       KEYWORDS
       ============================================================ */

    private Set<String> extractKeywords(
            String text) {

        Set<String> stopWords =
                new HashSet<>(
                        Arrays.asList(

                                "a",
                                "an",
                                "and",
                                "are",
                                "as",
                                "at",
                                "be",
                                "been",
                                "but",
                                "by",
                                "can",
                                "could",
                                "did",
                                "do",
                                "does",
                                "for",
                                "from",
                                "had",
                                "has",
                                "have",
                                "he",
                                "her",
                                "here",
                                "hers",
                                "him",
                                "his",
                                "how",
                                "i",
                                "if",
                                "in",
                                "into",
                                "is",
                                "it",
                                "its",
                                "me",
                                "my",
                                "of",
                                "on",
                                "or",
                                "our",
                                "ours",
                                "she",
                                "should",
                                "so",
                                "some",
                                "than",
                                "that",
                                "the",
                                "their",
                                "theirs",
                                "them",
                                "then",
                                "there",
                                "these",
                                "they",
                                "this",
                                "those",
                                "to",
                                "too",
                                "was",
                                "we",
                                "were",
                                "what",
                                "when",
                                "where",
                                "which",
                                "who",
                                "why",
                                "will",
                                "with",
                                "would",
                                "you",
                                "your",
                                "yours",
                                "according"
                        )
                );


        Set<String> keywords =
                new HashSet<>();


        if (text == null
                || text.isBlank()) {

            return keywords;
        }


        String normalized =
                normalize(text);


        String[] words =
                normalized.split(
                        "\\s+"
                );


        for (String word : words) {

            if (word == null
                    || word.isBlank()
                    || word.length() < 2) {

                continue;
            }


            if (stopWords.contains(
                    word
            )) {

                continue;
            }


            keywords.add(word);


            /*
             * Singular/plural.
             */
            if (word.equals("policies")) {
                keywords.add("policy");
            }


            if (word.equals("policy")) {
                keywords.add("policies");
            }


            if (word.endsWith("ies")
                    && word.length() > 4) {

                keywords.add(
                        word.substring(
                                0,
                                word.length() - 3
                        ) + "y"
                );
            }


            if (word.endsWith("s")
                    && word.length() > 3) {

                keywords.add(
                        word.substring(
                                0,
                                word.length() - 1
                        )
                );
            }
        }


        /*
         * Intent expansion.
         */

        addSynonyms(
                keywords,
                "supplier",
                "supplier",
                "suppliers",
                "vendor",
                "vendors",
                "associate",
                "associates",
                "business"
        );


        addSynonyms(
                keywords,
                "discount",
                "discount",
                "discounts",
                "benefit",
                "benefits",
                "advantage",
                "advantages",
                "preferential",
                "privilege",
                "privileges",
                "favour",
                "favor"
        );


        addSynonyms(
                keywords,
                "holiday",
                "holiday",
                "holidays",
                "vacation",
                "vacations",
                "travel",
                "hotel",
                "hotels",
                "hospitality",
                "accommodation"
        );


        addSynonyms(
                keywords,
                "booking",
                "booking",
                "bookings",
                "reservation",
                "reservations"
        );


        addSynonyms(
                keywords,
                "accept",
                "accept",
                "accepted",
                "acceptance",
                "receive",
                "received",
                "receiving",
                "offer",
                "offered"
        );


        return keywords;
    }


    private void addSynonyms(
            Set<String> keywords,
            String... values) {

        if (values.length == 0) {
            return;
        }


        String base =
                values[0];


        boolean found =
                false;


        for (String value :
                values) {

            if (keywords.contains(
                    value
            )) {

                found = true;
                break;
            }
        }


        if (found) {

            keywords.addAll(
                    Arrays.asList(
                            values
                    )
            );
        }
    }


    /* ============================================================
       KEYWORD SCORE
       ============================================================ */

    private double calculateKeywordScore(
            Set<String> keywords,
            String content) {

        if (keywords == null
                || keywords.isEmpty()
                || content == null
                || content.isBlank()) {

            return 0.0;
        }


        int matched = 0;


        for (String keyword :
                keywords) {

            if (containsWord(
                    content,
                    keyword
            )) {

                matched++;
            }
        }


        if (matched == 0) {
            return 0.0;
        }


        /*
         * Prevent synonym expansion from
         * unfairly destroying the score.
         */
        return Math.min(
                1.0,
                (double) matched /
                        Math.max(
                                3,
                                Math.min(
                                        keywords.size(),
                                        8
                                )
                        )
        );
    }


    /* ============================================================
       PHRASE SCORE
       ============================================================ */

    private double calculatePhraseScore(
            String question,
            String content) {

        if (question == null
                || question.isBlank()
                || content == null
                || content.isBlank()) {

            return 0.0;
        }


        String q =
                normalize(question);

        String c =
                normalize(content);


        if (q.length() >= 5
                && c.contains(q)) {

            return 1.0;
        }


        String[] words =
                q.split("\\s+");


        if (words.length < 2) {
            return 0.0;
        }


        int matches = 0;
        int possible = 0;


        for (
                int i = 0;
                i < words.length - 1;
                i++
        ) {

            String first =
                    words[i];

            String second =
                    words[i + 1];


            if (
                    first.length() < 3
                            || second.length() < 3
            ) {
                continue;
            }


            possible++;


            String phrase =
                    first
                            + " "
                            + second;


            if (c.contains(
                    phrase
            )) {

                matches++;
            }
        }


        if (possible == 0) {
            return 0.0;
        }


        return Math.min(
                1.0,
                (double) matches /
                        possible
        );
    }


    /* ============================================================
       CONCEPT SCORE
       ============================================================ */

    private double calculateConceptScore(
            Set<String> keywords,
            String content) {

        if (keywords == null
                || keywords.isEmpty()
                || content == null
                || content.isBlank()) {

            return 0.0;
        }


        String normalizedContent =
                normalize(content);


        Map<String, Set<String>>
                concepts =
                new HashMap<>();


        concepts.put(
                "gift",
                set(
                        "gift",
                        "gifts",
                        "present",
                        "presents",
                        "hospitality",
                        "benefit",
                        "benefits"
                )
        );


        concepts.put(
                "supplier",
                set(
                        "supplier",
                        "suppliers",
                        "vendor",
                        "vendors",
                        "business",
                        "businesses",
                        "associate",
                        "associates",
                        "provider",
                        "providers"
                )
        );


        concepts.put(
                "discount",
                set(
                        "discount",
                        "discounts",
                        "benefit",
                        "benefits",
                        "advantage",
                        "advantages",
                        "preferential",
                        "privilege",
                        "privileges",
                        "favour",
                        "favor",
                        "concession"
                )
        );


        concepts.put(
                "travel",
                set(
                        "holiday",
                        "holidays",
                        "vacation",
                        "vacations",
                        "travel",
                        "travelling",
                        "traveling",
                        "hotel",
                        "hotels",
                        "accommodation",
                        "hospitality",
                        "entertainment",
                        "booking",
                        "reservation"
                )
        );


        concepts.put(
                "personal",
                set(
                        "personal",
                        "private",
                        "employee",
                        "employees",
                        "individual",
                        "individuals"
                )
        );


        concepts.put(
                "acceptance",
                set(
                        "accept",
                        "accepted",
                        "acceptance",
                        "receive",
                        "received",
                        "receiving",
                        "offer",
                        "offered",
                        "offers",
                        "given",
                        "giving"
                )
        );


        concepts.put(
                "conflict",
                set(
                        "conflict",
                        "conflicts",
                        "interest",
                        "interests",
                        "personal",
                        "relationship",
                        "relationships"
                )
        );


        concepts.put(
                "conduct",
                set(
                        "conduct",
                        "ethics",
                        "ethical",
                        "code",
                        "integrity",
                        "principles",
                        "values",
                        "policy",
                        "policies"
                )
        );


        int conceptsInQuestion = 0;
        int conceptsMatched = 0;


        for (
                Set<String> conceptTerms
                : concepts.values()
        ) {

            boolean inQuestion =
                    false;


            for (String keyword :
                    keywords) {

                if (conceptTerms.contains(
                        keyword
                )) {

                    inQuestion = true;
                    break;
                }
            }


            if (!inQuestion) {
                continue;
            }


            conceptsInQuestion++;


            boolean inContent =
                    false;


            for (String term :
                    conceptTerms) {

                if (containsWord(
                        normalizedContent,
                        term
                )) {

                    inContent = true;
                    break;
                }
            }


            if (inContent) {
                conceptsMatched++;
            }
        }


        if (conceptsInQuestion == 0) {
            return 0.0;
        }


        return Math.min(
                1.0,
                (double) conceptsMatched /
                        conceptsInQuestion
        );
    }


    private Set<String> set(
            String... values) {

        return new HashSet<>(
                Arrays.asList(
                        values
                )
        );
    }


    /* ============================================================
       VECTOR SIMILARITY
       ============================================================ */

    private double calculateVectorSimilarity(
            List<Double> queryVector,
            List<Double> documentVector) {

        if (queryVector == null
                || documentVector == null
                || queryVector.isEmpty()
                || documentVector.isEmpty()) {

            return 0.0;
        }


        int size =
                Math.min(
                        queryVector.size(),
                        documentVector.size()
                );


        double dotProduct = 0.0;
        double queryMagnitude = 0.0;
        double documentMagnitude = 0.0;


        for (
                int i = 0;
                i < size;
                i++
        ) {

            Double q =
                    queryVector.get(i);

            Double d =
                    documentVector.get(i);


            if (q == null || d == null) {
                continue;
            }


            dotProduct +=
                    q * d;

            queryMagnitude +=
                    q * q;

            documentMagnitude +=
                    d * d;
        }


        if (
                queryMagnitude == 0.0
                        || documentMagnitude == 0.0
        ) {

            return 0.0;
        }


        double similarity =
                dotProduct /
                        (
                                Math.sqrt(
                                        queryMagnitude
                                )
                                        *
                                        Math.sqrt(
                                                documentMagnitude
                                        )
                        );


        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        similarity
                )
        );
    }


    /* ============================================================
       EVIDENCE
       ============================================================ */

    private boolean hasEvidence(
            double keywordScore,
            double phraseScore,
            double conceptScore,
            double vectorScore,
            double finalScore) {

        if (phraseScore >= 0.50) {
            return true;
        }


        if (conceptScore >= 0.40) {
            return true;
        }


        if (
                keywordScore > 0.0
                        && finalScore >= MIN_SCORE
        ) {

            return true;
        }


        return vectorScore >= 0.65;
    }


    private boolean isStrongEnough(
            SearchResult result) {

        if (result == null) {
            return false;
        }


        if (
                result.keywordScore() > 0.0
                        && result.score() >= MIN_SCORE
        ) {

            return true;
        }


        if (
                result.phraseScore() >= 0.50
        ) {

            return true;
        }


        if (
                result.conceptScore() >= 0.40
        ) {

            return true;
        }


        return result.vectorScore() >= 0.65;
    }


    /* ============================================================
       WORD MATCH
       ============================================================ */

    private boolean containsWord(
            String text,
            String word) {

        if (text == null
                || text.isBlank()
                || word == null
                || word.isBlank()) {

            return false;
        }


        String normalizedText =
                normalize(text);

        String normalizedWord =
                normalize(word);


        return Pattern
                .compile(
                        "\\b"
                                + Pattern.quote(
                                normalizedWord
                        )
                                + "\\b"
                )
                .matcher(
                        normalizedText
                )
                .find();
    }


    private String safe(
            String value) {

        return value == null
                ? ""
                : value;
    }


    public record SearchResult(
            DocumentChunk chunk,
            double score,
            double keywordScore,
            double phraseScore,
            double conceptScore,
            double vectorScore
    ) {
    }
}