package com.verity.dto;

import java.util.List;

public class ChatResponse {

    private String answer;
    private String status;
    private List<SourceResponse> sources;

    public ChatResponse(
            String answer,
            String status,
            List<SourceResponse> sources) {

        this.answer = answer;
        this.status = status;
        this.sources = sources;
    }

    public String getAnswer() {
        return answer;
    }

    public String getStatus() {
        return status;
    }

    public List<SourceResponse> getSources() {
        return sources;
    }

    public record SourceResponse(
            String fileName,
            int pageNumber,
            String evidence,
            double score
    ) {
    }
}