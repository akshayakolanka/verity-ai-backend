package com.verity.model;

import org.springframework.data.annotation.Id;

public class Document {

    @Id
    private String id;

    private String fileName;
    private String content;
    private String uploadedBy;
    private String requiredRole;

    public Document() {
    }

    public Document(String fileName, String content, String uploadedBy) {
        this.fileName = fileName;
        this.content = content;
        this.uploadedBy = uploadedBy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}