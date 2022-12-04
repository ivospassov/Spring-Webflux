package com.example.springwebfluxmongodb.exceptions;

public class ReviewDataException extends RuntimeException {
    private String message;

    public ReviewDataException(String errorMessage) {
        super(errorMessage);
        this.message = errorMessage;
    }
}
