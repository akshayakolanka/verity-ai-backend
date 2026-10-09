package com.verity.dto;

public class AskQuestionRequest {

    private String question;

    private String email;


    public AskQuestionRequest() {
    }


    public String getQuestion() {
        return question;
    }


    public void setQuestion(String question) {
        this.question = question;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }
}