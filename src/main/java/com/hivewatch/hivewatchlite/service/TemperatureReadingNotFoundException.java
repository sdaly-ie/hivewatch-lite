package com.hivewatch.hivewatchlite.service;

public class TemperatureReadingNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TemperatureReadingNotFoundException(String message) {
        super(message);
    }
}