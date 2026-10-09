package com.verity.service;

import com.verity.model.Document;
import com.verity.model.DocumentChunk;
import com.verity.repository.DocumentChunkRepository;
import com.verity.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentIngestionService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final PdfService pdfService;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;

    public DocumentIngestionService(
            DocumentRepository documentRepository,
            DocumentChunkRepository chunkRepository,
            PdfService pdfService,
            ChunkingService chunkingService,
            EmbeddingService embeddingService) {

        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.pdfService = pdfService;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
    }

    public Document ingest(
            MultipartFile file,
            String uploadedBy,
            String requiredRole
    ) throws IOException {

        String documentId = UUID.randomUUID().toString();

        // 1. Extract text from every PDF page
        List<PdfService.PageText> pages =
                pdfService.extractPages(file);

        // 2. Combine all page text for the main document
        StringBuilder completeText = new StringBuilder();

        for (PdfService.PageText page : pages) {
            completeText
                    .append(page.text())
                    .append("\n\n");
        }

        // 3. Save the original document information
        Document document = new Document(
                file.getOriginalFilename(),
                completeText.toString(),
                uploadedBy
        );

        document.setId(documentId);
        document.setRequiredRole(requiredRole);

        documentRepository.save(document);

        // 4. Create chunks and embeddings
        for (PdfService.PageText page : pages) {

            List<String> chunks =
                    chunkingService.chunk(page.text());

            for (String chunk : chunks) {

                // Convert chunk into an embedding vector
                List<Double> embedding =
                        embeddingService.generateEmbedding(chunk);

                DocumentChunk documentChunk =
                        new DocumentChunk(
                                documentId,
                                file.getOriginalFilename(),
                                chunk,
                                page.pageNumber(),
                                uploadedBy,
                                requiredRole,
                                embedding
                        );

                // 5. Save chunk + embedding
                chunkRepository.save(documentChunk);
            }
        }

        return document;
    }
}