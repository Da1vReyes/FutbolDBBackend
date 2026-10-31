package edu.uam.futboldb.player;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /** Llena la base de jugadores buscando por nombre en TheSportsDB. */
    @PostMapping("/import/{nombre}")
    public Map<String, Object> importar(@PathVariable String nombre) {
        int importados = playerService.importarJugadoresPorNombre(nombre);
        return Map.of("busqueda", nombre, "importados", importados);
    }
}
