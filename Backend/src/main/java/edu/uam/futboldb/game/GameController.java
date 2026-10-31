package edu.uam.futboldb.game;

import edu.uam.futboldb.game.GameDtos.NuevaPartidaResponse;
import edu.uam.futboldb.game.GameDtos.RespuestaRequest;
import edu.uam.futboldb.game.GameDtos.ResultadoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /** Crea una partida con un jugador al azar y devuelve el gameId junto con dos pistas. */
    @PostMapping("/nueva")
    @ResponseStatus(HttpStatus.CREATED)
    public NuevaPartidaResponse nueva(Authentication authentication) {
        return gameService.nuevaPartida(authentication.getName());
    }

    /** Evalúa las respuestas enviadas; solo los campos no vacíos cuentan como intento de acierto. */
    @PostMapping("/{gameId}/responder")
    public ResultadoResponse responder(@PathVariable Long gameId, @RequestBody RespuestaRequest respuesta,
                                       Authentication authentication) {
        return gameService.responder(gameId, authentication.getName(), respuesta);
    }
}
