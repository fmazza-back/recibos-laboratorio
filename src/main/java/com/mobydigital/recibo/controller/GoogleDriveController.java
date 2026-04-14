package com.mobydigital.recibo.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.drive.model.File;
import com.mobydigital.recibo.controller.dto.ReciboListResponse;
import com.mobydigital.recibo.exception.InvalidAuthHeaderException;
import com.mobydigital.recibo.service.GoogleDriveService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recibos")
@RequiredArgsConstructor
public class GoogleDriveController {

    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveController.class);

    private final GoogleDriveService driveService;

    @GetMapping
    public ResponseEntity<ReciboListResponse> listarRecibos(
            @RequestHeader("Authorization") String authHeader) throws GeneralSecurityException, IOException {

        String accessToken = getGoogleAccessToken(authHeader);
        List<File> recibos = driveService.listarRecibos(accessToken);
        logger.info("Creando respuesta con {} recibos.", recibos.size());
        return ResponseEntity.ok(new ReciboListResponse(true, "Recibos obtenidos correctamente.", recibos.size(), recibos, System.currentTimeMillis()));
    }

    private String getGoogleAccessToken(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            logger.warn("El encabezado 'Authorization' está ausente o vacío.");
            throw new InvalidAuthHeaderException("Autorización requerida. Encabezado 'Authorization' no encontrado.");
        }

        logger.debug("Encabezado 'Authorization' recibido: {}", authHeader);

        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("El encabezado 'Authorization' no es del tipo Bearer: '{}'", authHeader);
            throw new InvalidAuthHeaderException("Autorización requerida. El encabezado 'Authorization' debe ser del tipo Bearer.");
        }

        String token = authHeader.substring(7);
        logger.info("Access token extraído correctamente (primeros 10 chars: {}...)", token.length() > 10 ? token.substring(0, 10) : token);
        return token;
    }

}
