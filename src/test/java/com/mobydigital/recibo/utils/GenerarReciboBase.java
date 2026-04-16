package com.mobydigital.recibo.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import java.io.IOException;

public class GenerarReciboBase {

    public static void main(String[] args) {
        String nombreArchivo = "recibo_vacio.pdf";

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                // CONFIGURACIÓN DE FUENTES PARA PDFBOX 3.x
                PDType1Font helveticaBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font helveticaNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                // Titulo
                contentStream.beginText();
                contentStream.setFont(helveticaBold, 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("RECIBO DE SUELDO - LABORATORIO");
                contentStream.endText();

                // Información del Empleado
                contentStream.beginText();
                contentStream.setFont(helveticaNormal, 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Empleado: Fulano (fula)");
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Legajo: 98765");
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Periodo: Abril 2026");
                contentStream.endText();

                // Cuadro de haberes
                contentStream.setLineWidth(1f);
                contentStream.addRect(50, 450, 500, 200);
                contentStream.stroke();

                contentStream.beginText();
                contentStream.setFont(helveticaNormal, 10);
                contentStream.newLineAtOffset(60, 630);
                contentStream.showText("Concepto: Haberes Mensuales ........................ $500.000");
                contentStream.endText();

                // --- LÍNEA DE GUÍA PARA LA FIRMA ---
                // X=420, Y=185 (La línea donde apoyará la firma)
                contentStream.moveTo(420, 185);
                contentStream.lineTo(570, 185);
                contentStream.stroke();

                contentStream.beginText();
                contentStream.setFont(helveticaBold, 9);
                contentStream.newLineAtOffset(450, 170);
                contentStream.showText("FIRMA EMPLEADO");
                contentStream.endText();
            }

            document.save(nombreArchivo);
            System.out.println("✅ Archivo '" + nombreArchivo + "' generado con éxito.");

        } catch (IOException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}