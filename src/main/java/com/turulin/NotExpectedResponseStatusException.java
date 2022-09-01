package com.turulin;

public class NotExpectedResponseStatusException extends Exception {
    public NotExpectedResponseStatusException (String message) {
        super(message);
    }
}
