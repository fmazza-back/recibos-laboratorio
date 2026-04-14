package com.mobydigital.recibo.service;

import com.mobydigital.recibo.controller.dto.FirebaseFirmaResponse;
import com.mobydigital.recibo.exception.FirmaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // 1. Habilita Mockito para JUnit 5
class FirmaServiceTest {

    @Mock // 2. Crea un "falso" RestTemplate
    private RestTemplate restTemplate;

    @InjectMocks // 3. Mete ese "falso" RestTemplate dentro del Service
    private FirmaService firmaService;

    private byte[] pdfVacio;

    @BeforeEach
    void setUp() {
        // Preparamos un PDF de prueba (un array de bytes cualquiera)
        pdfVacio = "Contenido PDF".getBytes();
    }

    @Test
    void firmarDocumento_DebeLanzarException_CuandoFirmaNoEstaValidada() {
        // ARRANGE (Preparar)
        // Simulamos que el sistema  devuelve una firma "PENDIENTE"
        FirebaseFirmaResponse fakeResponse = new FirebaseFirmaResponse();
        fakeResponse.setEstado("PENDIENTE");


        when(restTemplate.getForObject(anyString(), eq(FirebaseFirmaResponse.class)))
                .thenReturn(fakeResponse);

        // ACT & ASSERT (Actuar y Verificar)
        // Verificamos que al llamar salte la excepcion
        assertThrows(FirmaException.class, () -> {
            firmaService.firmarDocumento(pdfVacio, "usuario123");
        });
    }
}