package com.mobydigital.recibo.runner;


import com.mobydigital.recibo.service.FirmaService;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FirmaLocalRunner {
    public static void main(String[] args) {
        try {
            // PRUEBA REAL FIRMADO DE ARCHIVOS
            //Cargamos lo necesario de forma local
            byte[] pdfBytes = Files.readAllBytes(Paths.get("recibo_vacio.pdf"));
            byte[] firmaBytes = Files.readAllBytes(Paths.get("descarga.png"));

            // 2. Instanciamos el servicio (pasamos null al RestTemplate porque no lo usaremos)
            FirmaService service = new FirmaService(null);

            // 3. Ejecutamos el metodo de firmar
            byte[] resultado = service.aplicarFirma(pdfBytes, firmaBytes, "Facu_Test");

            // 4. Guardamos el resultado para visualizarlo
            try (FileOutputStream fos = new FileOutputStream("RECIBO_FIRMADO_FINAL.pdf")) {
                fos.write(resultado);
            }

            System.out.println("✅ ¡Proceso completado! Abrí 'RECIBO_FIRMADO_FINAL.pdf' para ver el resultado.");

        } catch (Exception e) {
            System.err.println("❌ Falló la prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
}