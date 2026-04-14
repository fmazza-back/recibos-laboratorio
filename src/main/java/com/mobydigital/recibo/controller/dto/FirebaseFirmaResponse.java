package com.mobydigital.recibo.controller.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseFirmaResponse {

    @JsonProperty("usuario_id")
    private String usuarioId;

    private String estado;

    @JsonProperty("url_firma")
    private String urlFirma;
}