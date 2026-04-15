package com.mobydigital.recibo.service;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirmaProcesoService {

    private static final Logger logger = LoggerFactory.getLogger(FirmaProcesoService.class);

    private final GoogleDriveService driveService;
    private final FirmaService firmaService;

   //Flujo: descargar, firmar, reemplazar
    public void ejecutarFirma(String accessToken, String fileId, String usuarioId) throws Exception {

        // 1. Descargar el archivo desde Drive
        byte[] pdfOriginal = driveService.descargar(accessToken, fileId);

        // 2. Procesar la firma
        // (FirmaService da las coordenadas X,Y)
        byte[] pdfFirmado = firmaService.firmarDocumento(pdfOriginal, usuarioId);

        // 3. Reemplazar el archivo en Drive con la versión firmada
        driveService.reemplazar(accessToken, fileId, pdfFirmado);

        logger.info("Documento {} firmado y actualizado en Drive para el usuario {}.", fileId, usuarioId);
    }
}