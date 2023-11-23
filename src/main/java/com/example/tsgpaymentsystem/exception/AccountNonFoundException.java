package com.example.tsgpaymentsystem.exception;

public class AccountNonFoundException extends Exception{
    public AccountNonFoundException(String s) {
        super("Аккаунт " + s + " не найден");
    }
}
