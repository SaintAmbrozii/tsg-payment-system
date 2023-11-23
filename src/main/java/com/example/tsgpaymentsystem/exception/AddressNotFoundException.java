package com.example.tsgpaymentsystem.exception;

public class AddressNotFoundException extends Exception{
    public AddressNotFoundException(String account, String address) {
        super("Не возможно найти адрес " + address + " привязанный к аккаунту " + account);
    }
}
