package com.example.tsgpaymentsystem.exception;

public class UserAlreadyExistException extends RuntimeException{
    public UserAlreadyExistException(String s) {
        super(s);
    }
}
