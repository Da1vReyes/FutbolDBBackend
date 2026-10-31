package edu.uam.futboldb.game;

import edu.uam.futboldb.player.Player;

import java.util.function.Function;

/** Campos del jugador que se muestran como pista y que el usuario debe adivinar. */
public enum Campo {
    EDAD("edad", p -> String.valueOf(p.getEdad())),
    PAIS_REGION("paisRegion", Player::getPaisRegion),
    POSICION("posicion", Player::getPosicion),
    LIGA("liga", Player::getLiga),
    EQUIPO_ACTUAL("equipoActual", Player::getEquipoActual);

    private final String clave;
    private final Function<Player, String> extractor;

    Campo(String clave, Function<Player, String> extractor) {
        this.clave = clave;
        this.extractor = extractor;
    }

    /** Nombre del campo en el JSON de la API. */
    public String clave() {
        return clave;
    }

    public String valorDe(Player jugador) {
        String valor = extractor.apply(jugador);
        return valor != null ? valor : "";
    }
}
