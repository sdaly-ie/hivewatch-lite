package com.hivewatch.hivewatchlite.service;

public class HiveNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public HiveNotFoundException(String message) {
        super(message);
    }
}