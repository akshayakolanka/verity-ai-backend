package com.verity.service;

import com.verity.model.Document;
import com.verity.repository.DocumentRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;


    public DocumentService(
            DocumentRepository documentRepository) {

        this.documentRepository =
                documentRepository;
    }


    public List<Document> getDocumentsForUser(
            String email) {

        if (email == null
                || email.isBlank()) {

            return List.of();
        }


        return documentRepository
                .findByUploadedByIgnoreCase(
                        email
                );
    }


    public Document saveDocument(
            Document document) {

        return documentRepository.save(
                document
        );
    }
}