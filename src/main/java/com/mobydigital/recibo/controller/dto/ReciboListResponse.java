package com.mobydigital.recibo.controller.dto;

import java.util.List;

public record ReciboListResponse(
        boolean success,
        String message,
        int count,
        List<ReciboDTO> data,
        long timestamp
) {}
