package com.example.springwebfluxmongodb.exceptions;

public class MovieInfoServerException extends RuntimeException{
    private String message;

    public MovieInfoServerException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
