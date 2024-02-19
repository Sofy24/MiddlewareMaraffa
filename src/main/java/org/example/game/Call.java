package org.example.game;

public enum Call {
    BUSSO, STRISCIO_LUNGO, STRISCIO_CORTO, VOLO, NONE;

    public static Call fromUppercaseString(String call) {
        return switch (call) {
            case "BUSSO" -> BUSSO;
            case "STRISCIO_LUNGO" -> STRISCIO_LUNGO;
            case "STRISCIO_CORTO" -> STRISCIO_CORTO;
            case "VOLO" -> VOLO;
            default -> NONE;
        };
    }
}
