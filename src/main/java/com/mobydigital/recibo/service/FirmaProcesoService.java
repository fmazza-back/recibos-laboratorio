package com.mobydigital.recibo.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirmaProcesoService {

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

        System.out.println("Documento " + fileId + " firmado y actualizado en Drive para el usuario " + usuarioId);
    }
}