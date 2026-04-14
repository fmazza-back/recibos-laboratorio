package com.mobydigital.recibo.exception;


//Excepción personalizada para errores específicos del proceso de firmado.

public class FirmaException extends RuntimeException {

    public FirmaException(String mensaje) {
        super(mensaje);
    }

    public FirmaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}