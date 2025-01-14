package com.bizfns.services.Exceptions;

public class RecurrExceptions extends RuntimeException{

    private String massage;

    public RecurrExceptions() {

    }

    public RecurrExceptions(String message) {
        super(message);
        this.massage = message;
    }
}
