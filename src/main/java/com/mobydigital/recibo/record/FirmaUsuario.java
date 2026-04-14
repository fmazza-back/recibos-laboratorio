package com.mobydigital.recibo.record;

import java.time.LocalDateTime;

public record FirmaUsuario(
        String usuarioId,
        byte[] imagenBytes, // Imagen en bytes
        String tipoMime,    // "Imagen en jpeg/ png"
        LocalDateTime fechaCreacion
) {}
