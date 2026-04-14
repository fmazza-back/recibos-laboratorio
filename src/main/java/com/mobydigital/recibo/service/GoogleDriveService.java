package com.mobydigital.recibo.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.mobydigital.recibo.config.GoogleDriveConfig;
import com.mobydigital.recibo.controller.dto.ReciboDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleDriveService {

    private final GoogleDriveConfig driveConfig;

    @Value("${google.drive.folder-id}")
    private String folderId;

    /**
     * Lista los ultimos 4 PDFs subidos a la carpeta de recibos.
     */
    public List<ReciboDTO> listarRecibos(String accessToken) throws GeneralSecurityException, IOException {
        Drive drive = driveConfig.buildClient(accessToken);

        String query = String.format(
                "'%s' in parents and mimeType = 'application/pdf' and trashed = false",
                folderId
        );

        FileList result = drive.files().list()
                .setQ(query)
                .setFields("files(id, name, createdTime, webViewLink)")
                .setOrderBy("createdTime desc")
                .setPageSize(4)
                .execute();

        List<File> files = result.getFiles();
        if (files == null) return List.of();

        return files.stream()
                .map(f -> new ReciboDTO(
                        f.getId(),
                        f.getName(),
                        f.getCreatedTime() != null ? f.getCreatedTime().toStringRfc3339() : null,
                        f.getWebViewLink()
                ))
                .toList();
    }

    /**
     * Busca un recibo especifico por su ID de Drive.
     */
    public ReciboDTO buscarPorId(String accessToken, String fileId) throws GeneralSecurityException, IOException {
        Drive drive = driveConfig.buildClient(accessToken);
        File file = drive.files().get(fileId)
                .setFields("id, name, createdTime, webViewLink")
                .execute();
        return new ReciboDTO(file.getId(), file.getName(),
                file.getCreatedTime() != null ? file.getCreatedTime().toStringRfc3339() : null,
                file.getWebViewLink());
    }

    /**
     * Descarga el contenido de un archivo de Drive como bytes.
     */
    public byte[] descargar(String accessToken, String fileId) throws GeneralSecurityException, IOException {
        Drive drive = driveConfig.buildClient(accessToken);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        drive.files().get(fileId).executeMediaAndDownloadTo(outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Reemplaza el contenido de un archivo existente en Drive.
     */
    public void reemplazar(String accessToken, String fileId, byte[] contenido) throws GeneralSecurityException, IOException {
        Drive drive = driveConfig.buildClient(accessToken);
        ByteArrayContent mediaContent = new ByteArrayContent("application/pdf", contenido);
        drive.files().update(fileId, new File(), mediaContent).execute();
    }
}
