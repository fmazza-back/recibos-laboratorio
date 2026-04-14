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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleDriveService {

    private final GoogleDriveConfig driveConfig;

    @Value("${google.drive.folder-id}")
    private String folderId;

    /**
     * Lista todos los PDFs de la carpeta de recibos.
     */
    public List<File> listarRecibos(String accessToken) throws GeneralSecurityException, IOException {
        Drive drive = driveConfig.buildClient(accessToken);

        String query = String.format(
                "'%s' in parents and mimeType = 'application/pdf' and trashed = false",
                folderId
        );

        FileList result = drive.files().list()
                .setQ(query)
                .setFields("files(id, name, createdTime, webViewLink)")
                .setOrderBy("name")
                .execute();

        List<File> files = result.getFiles();
        return files != null ? files : List.of();
    }

    /**
     * Busca un recibo especifico por su ID de Drive.
     */
    public File buscarPorId(String accessToken, String fileId) throws GeneralSecurityException, IOException {
        Drive drive = driveConfig.buildClient(accessToken);
        return drive.files().get(fileId)
                .setFields("id, name, webViewLink")
                .execute();
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
