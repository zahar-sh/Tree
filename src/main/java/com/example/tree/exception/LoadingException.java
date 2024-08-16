package com.example.tree.exception;

public class LoadingException extends Exception {

    public LoadingException() {
        super();
    }

    public LoadingException(String message) {
        super(message);
    }

    public LoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadingException(Throwable cause) {
        super(cause);
    }
}
