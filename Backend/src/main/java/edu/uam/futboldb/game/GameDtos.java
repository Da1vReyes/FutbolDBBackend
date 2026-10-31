package edu.uam.futboldb.game;

import java.util.List;
import java.util.Map;

public final class GameDtos {

    private GameDtos() {
    }

    public record Pista(String campo, String valor) {
    }

    public record NuevaPartidaResponse(Long gameId, List<Pista> pistas, String descripcion) {
    }

    /** Respuesta del usuario: los campos que deje vacíos no se evalúan. */
    public record RespuestaRequest(Integer edad, String paisRegion, String posicion, String liga, String equipoActual) {
    }

    public record ResultadoResponse(
            boolean finalizada,
            int intentos,
            String mensaje,
            List<String> acertadosEstaRonda,
            List<String> incorrectos,
            List<String> faltantes,
            Map<String, String> valoresAcertados,
            String jugador) {
    }
}
