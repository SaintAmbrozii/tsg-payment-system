package com.example.tsgpaymentsystem.exception;

import java.io.IOException;

public class FileEmptyException extends IOException {
    public FileEmptyException(String s) {
        super(s);
    }
}
