package com.verity.repository;

import com.verity.model.DocumentChunk;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DocumentChunkRepository extends MongoRepository<DocumentChunk, String> {

    List<DocumentChunk> findByDocumentId(String documentId);
}