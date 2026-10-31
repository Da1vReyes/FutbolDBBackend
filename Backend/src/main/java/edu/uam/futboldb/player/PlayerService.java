package edu.uam.futboldb.player;

import edu.uam.futboldb.player.thesportsdb.PlayerApiContainer;
import edu.uam.futboldb.player.thesportsdb.PlayerApiResponseDTO;
import edu.uam.futboldb.player.thesportsdb.TeamApiContainer;
import edu.uam.futboldb.player.thesportsdb.TeamApiResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

@Service
public class PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);
    private static final String API_HOST = "https://www.thesportsdb.com/api/v1/json/";
    private static final String DESCONOCIDA = "Desconocida";

    private final PlayerRepository playerRepository;
    private final String apiBase;
    private final RestTemplate restTemplate = new RestTemplate();

    public PlayerService(PlayerRepository playerRepository,
                         @Value("${app.thesportsdb.api-key}") String apiKey) {
        this.playerRepository = playerRepository;
        this.apiBase = API_HOST + apiKey;
    }

    /** Importa de TheSportsDB los jugadores que coinciden con el nombre y devuelve cuántos guardó. */
    public int importarJugadoresPorNombre(String nombreJugador) {
        URI uri = UriComponentsBuilder.fromHttpUrl(apiBase + "/searchplayers.php")
                .queryParam("p", nombreJugador)
                .build().encode().toUri();
        PlayerApiContainer body = restTemplate.getForObject(uri, PlayerApiContainer.class);
        if (body == null || body.getPlayer() == null) {
            return 0;
        }

        int importados = 0;
        for (PlayerApiResponseDTO api : body.getPlayer()) {
            String equipo = api.getStrTeam() != null ? api.getStrTeam() : "Desconocido";
            if (api.getStrPlayer() == null || playerRepository.existsByPlayerNameAndEquipoActual(api.getStrPlayer(), equipo)) {
                continue;
            }
            playerRepository.save(Player.builder()
                    .playerName(api.getStrPlayer())
                    .edad(calcularEdad(api.getDateBorn()))
                    .paisRegion(api.getStrNationality() != null ? api.getStrNationality() : DESCONOCIDA)
                    .posicion(Posicion.traducirDesdeApi(api.getStrPosition()))
                    .liga(obtenerLiga(api.getIdTeam()))
                    .equipoActual(equipo)
                    .build());
            importados++;
        }
        return importados;
    }

    private int calcularEdad(String fechaNacimiento) {
        if (fechaNacimiento == null || fechaNacimiento.isBlank()) return 0;
        try {
            return Period.between(LocalDate.parse(fechaNacimiento), LocalDate.now()).getYears();
        } catch (DateTimeParseException e) {
            return 0;
        }
    }

    private String obtenerLiga(String idTeam) {
        if (idTeam == null || idTeam.isBlank()) return DESCONOCIDA;
        URI uri = UriComponentsBuilder.fromHttpUrl(apiBase + "/lookupteam.php")
                .queryParam("id", idTeam)
                .build().encode().toUri();
        try {
            TeamApiContainer body = restTemplate.getForObject(uri, TeamApiContainer.class);
            if (body != null && body.getTeams() != null && !body.getTeams().isEmpty()) {
                TeamApiResponseDTO team = body.getTeams().get(0);
                if (team.getStrLeague() != null) return team.getStrLeague();
            }
        } catch (RestClientException e) {
            log.warn("No se pudo obtener la liga del equipo {}", idTeam, e);
        }
        return DESCONOCIDA;
    }
}
