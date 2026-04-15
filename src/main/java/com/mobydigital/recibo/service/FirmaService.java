package com.mobydigital.recibo.service;

import com.mobydigital.recibo.controller.dto.FirebaseFirmaResponse;
import com.mobydigital.recibo.exception.FirmaException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class FirmaService {

    private final RestTemplate restTemplate;

    // Coords fijas para la firma (ajustar segun necesidad)
    private static final float X_POSITION = 400;
    private static final float Y_POSITION = 100;
    private static final float ANCHO_DESEADO = 150;

    //Obtener la firma de Firebase
    public byte[] firmarDocumento(byte[] pdfBytes, String usuarioId) throws Exception {

        // 1. Pedir la info de la firma al endpoint
        String urlApi = "https://un-link-de-prueba.com/firma_braian.png" + usuarioId;
        FirebaseFirmaResponse response;

        try {
            response = restTemplate.getForObject(urlApi, FirebaseFirmaResponse.class);
        } catch (Exception e) {
            throw new FirmaException("No se pudo conectar con el servicio de firmas de Firebase.");
        }

        // 2. Validar el estado
        if (response == null || !"VALIDADA".equals(response.getEstado())) {
            throw new FirmaException("El usuario no tiene una firma validada en el sistema.");
        }

        // 3. Descargar la imagen  desde la URL proporcionada
        byte[] imagenFirma = descargarUrl(response.getUrlFirma());

        // 4. Firmar
        return aplicarFirma(pdfBytes, imagenFirma, usuarioId);
    }

    private byte[] descargarUrl(String urlImagen) {
        try (InputStream in = new URL(urlImagen).openStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new FirmaException("Error al descargar la imagen de la firma desde el storage.");
        }
    }

    private byte[] aplicarFirma(byte[] pdfBytes, byte[] firmaBytes, String id) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDPage page = document.getPage(document.getNumberOfPages() - 1);

            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, firmaBytes, "firma_" + id);

            float altoProporcional = (pdImage.getHeight() / (float) pdImage.getWidth()) * ANCHO_DESEADO;

            try (PDPageContentStream contentStream = new PDPageContentStream(
                    document, page, PDPageContentStream.AppendMode.APPEND, true)) {
                contentStream.drawImage(pdImage, X_POSITION, Y_POSITION, ANCHO_DESEADO, altoProporcional);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}