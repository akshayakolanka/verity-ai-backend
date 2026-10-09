package com.verity.model;

import org.springframework.data.annotation.Id;

import java.util.List;

public class DocumentChunk {

    @Id
    private String id;

    private String documentId;
    private String fileName;
    private String content;
    private int pageNumber;
    private String uploadedBy;
    private String requiredRole;
    private List<Double> embedding;

    public DocumentChunk() {
    }

    public DocumentChunk(
            String documentId,
            String fileName,
            String content,
            int pageNumber,
            String uploadedBy,
            String requiredRole,
            List<Double> embedding
    ) {
        this.documentId = documentId;
        this.fileName = fileName;
        this.content = content;
        this.pageNumber = pageNumber;
        this.uploadedBy = uploadedBy;
        this.requiredRole = requiredRole;
        this.embedding = embedding;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(String requiredRole) {
        this.requiredRole = requiredRole;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }
}