package com.mobydigital.recibo.exception;

public class InvalidAuthHeaderException extends RuntimeException {

    public InvalidAuthHeaderException(String message) {
        super(message);
    }
}
