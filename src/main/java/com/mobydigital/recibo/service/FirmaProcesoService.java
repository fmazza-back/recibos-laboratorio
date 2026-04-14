package com.mobydigital.recibo.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirmaProcesoService {

    private final GoogleDriveService driveService; // Lo de tu compañero
    private final FirmaService firmaService;       // Tu lógica

    /**
     * El flujo completo: descarga, firma y reemplaza.
     */
    public void ejecutarFirma(String accessToken, String fileId, String usuarioId) throws Exception {

        // 1. Descargar el archivo desde Drive usando el código de tu compañero
        byte[] pdfOriginal = driveService.descargar(accessToken, fileId);

        // 2. Procesar la firma con tu lógica
        // (Esto usa tu FirmaService que ya estampa en las coordenadas X,Y)
        byte[] pdfFirmado = firmaService.firmarDocumento(pdfOriginal, usuarioId);

        // 3. Reemplazar el archivo en Drive con la versión firmada
        driveService.reemplazar(accessToken, fileId, pdfFirmado);

        System.out.println("Documento " + fileId + " firmado y actualizado en Drive para el usuario " + usuarioId);
    }
}