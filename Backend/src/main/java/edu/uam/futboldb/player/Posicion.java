package edu.uam.futboldb.player;

import java.util.HashMap;
import java.util.Map;

public enum Posicion {
    POR("goalkeeper"),
    LD("right back"),
    CAD("right wing back"),
    DFC("centre-back"),
    LI("left back"),
    CAI("left wing back"),
    MCD("defensive midfield"),
    MC("central midfield"),
    MCO("attacking midfield"),
    MD("right midfield"),
    MI("left midfield"),
    ED("right winger"),
    EI("left winger"),
    SD("centre-forward"),
    DEL("striker");

    private static final Map<String, Posicion> POR_NOMBRE_API = new HashMap<>();

    static {
        for (Posicion pos : values()) {
            POR_NOMBRE_API.put(pos.apiName, pos);
        }
    }

    private final String apiName;

    Posicion(String apiName) {
        this.apiName = apiName;
    }

    /** Traduce la posición de TheSportsDB a su abreviatura; si no se reconoce, devuelve DEL. */
    public static String traducirDesdeApi(String apiPosition) {
        if (apiPosition == null) return DEL.name();
        Posicion pos = POR_NOMBRE_API.get(apiPosition.trim().toLowerCase());
        return pos != null ? pos.name() : DEL.name();
    }
}
