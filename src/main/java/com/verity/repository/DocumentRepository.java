package com.verity.repository;

import com.verity.model.Document;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DocumentRepository
        extends MongoRepository<Document, String> {

    List<Document> findByUploadedByIgnoreCase(
            String uploadedBy
    );
}