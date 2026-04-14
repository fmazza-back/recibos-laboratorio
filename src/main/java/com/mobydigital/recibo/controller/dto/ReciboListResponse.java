package com.mobydigital.recibo.controller.dto;

import com.google.api.services.drive.model.File;

import java.util.List;

public record ReciboListResponse(
        boolean success,
        String message,
        int count,
        List<File> data,
        long timestamp
) {}
