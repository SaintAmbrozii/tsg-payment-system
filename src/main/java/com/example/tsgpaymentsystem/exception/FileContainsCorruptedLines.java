package com.example.tsgpaymentsystem.exception;

import java.util.List;

public class FileContainsCorruptedLines extends RuntimeException{
    public FileContainsCorruptedLines(List<String> s) {
        super((Throwable) s);
    }
}
