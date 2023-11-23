package com.example.tsgpaymentsystem.exception;

public class InvalidAddressFormatException extends RuntimeException{

    public InvalidAddressFormatException(String s) {
        super("Адрес не правильного формата, принимаются адреса состоящие из 3 частей, разделенных запятой, например Новосибирск,Выборная,89/4,179, Ваш " + s);
    }

}
