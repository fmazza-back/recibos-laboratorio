package com.mobydigital.recibo.controller.dto;

public record FirmaResponse(
        boolean success,
        String message,
        long timestamp
) {}
