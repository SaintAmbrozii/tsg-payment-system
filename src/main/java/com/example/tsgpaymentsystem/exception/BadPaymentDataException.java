package com.example.tsgpaymentsystem.exception;

public class BadPaymentDataException extends RuntimeException{

    public BadPaymentDataException(String s) {
        super("Проверьте поле " + s);
    }

}
