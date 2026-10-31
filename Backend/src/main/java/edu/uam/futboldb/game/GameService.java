package edu.uam.futboldb.game;

import edu.uam.futboldb.exception.ApiException;
import edu.uam.futboldb.game.GameDtos.NuevaPartidaResponse;
import edu.uam.futboldb.game.GameDtos.Pista;
import edu.uam.futboldb.game.GameDtos.RespuestaRequest;
import edu.uam.futboldb.game.GameDtos.ResultadoResponse;
import edu.uam.futboldb.player.Player;
import edu.uam.futboldb.player.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.random.RandomGenerator;

@Service
public class GameService {

    private static final int PISTAS_INICIALES = 2;

    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final RandomGenerator random;

    @Autowired
    public GameService(PlayerRepository playerRepository, GameRepository gameRepository) {
        this(playerRepository, gameRepository, new Random());
    }

    GameService(PlayerRepository playerRepository, GameRepository gameRepository, RandomGenerator random) {
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.random = random;
    }

    @Transactional
    public NuevaPartidaResponse nuevaPartida(String usuarioEmail) {
        List<Player> jugadores = playerRepository.findAll();
        if (jugadores.isEmpty()) {
            throw ApiException.conflicto("No hay jugadores disponibles; impórtalos primero con /api/players/import/{nombre}");
        }
        Player jugador = jugadores.get(random.nextInt(jugadores.size()));

        Game partida = new Game();
        partida.setJugadorSeleccionado(jugador);
        partida.setUsuarioEmail(usuarioEmail);
        partida = gameRepository.save(partida);

        List<Campo> campos = new ArrayList<>(List.of(Campo.values()));
        Collections.shuffle(campos, new Random(random.nextLong()));
        List<Pista> pistas = campos.subList(0, PISTAS_INICIALES).stream()
                .map(c -> new Pista(c.clave(), c.valorDe(jugador)))
                .toList();

        return new NuevaPartidaResponse(partida.getId(), pistas,
                "Adivina al jugador a partir de las pistas. Cada intento indica qué campos has acertado.");
    }

    @Transactional
    public ResultadoResponse responder(Long gameId, String usuarioEmail, RespuestaRequest respuesta) {
        Game partida = gameRepository.findById(gameId)
                .orElseThrow(() -> ApiException.noEncontrado("Partida no encontrada"));
        if (!partida.getUsuarioEmail().equals(usuarioEmail)) {
            // Mismo error que si no existiera, para no revelar ids de partidas ajenas.
            throw ApiException.noEncontrado("Partida no encontrada");
        }
        if (partida.isFinalizada()) {
            throw ApiException.conflicto("La partida ya ha finalizado");
        }

        Player jugador = partida.getJugadorSeleccionado();
        Map<Campo, String> respuestas = respuestasPorCampo(respuesta);

        List<String> acertadosRonda = new ArrayList<>();
        List<String> incorrectos = new ArrayList<>();
        for (Campo campo : Campo.values()) {
            String dada = respuestas.get(campo);
            if (dada == null || dada.isBlank()) continue;
            if (dada.trim().equalsIgnoreCase(campo.valorDe(jugador).trim())) {
                if (partida.getCamposAcertados().add(campo)) {
                    acertadosRonda.add(campo.clave());
                }
            } else {
                incorrectos.add(campo.clave());
            }
        }

        partida.setIntentosRealizados(partida.getIntentosRealizados() + 1);
        boolean finalizada = partida.getCamposAcertados().size() == Campo.values().length;
        partida.setFinalizada(finalizada);
        gameRepository.save(partida);

        List<String> faltantes = Arrays.stream(Campo.values())
                .filter(c -> !partida.getCamposAcertados().contains(c))
                .map(Campo::clave)
                .toList();
        Map<String, String> valoresAcertados = new LinkedHashMap<>();
        for (Campo campo : Campo.values()) {
            if (partida.getCamposAcertados().contains(campo)) {
                valoresAcertados.put(campo.clave(), campo.valorDe(jugador));
            }
        }

        String mensaje = finalizada
                ? "¡Has acertado todos los campos en " + partida.getIntentosRealizados() + " intentos! El jugador es " + jugador.getPlayerName()
                : "Intento #" + partida.getIntentosRealizados() + " procesado. Sigue con los campos que faltan.";
        return new ResultadoResponse(finalizada, partida.getIntentosRealizados(), mensaje, acertadosRonda,
                incorrectos, faltantes, valoresAcertados, finalizada ? jugador.getPlayerName() : null);
    }

    private Map<Campo, String> respuestasPorCampo(RespuestaRequest r) {
        Map<Campo, String> mapa = new EnumMap<>(Campo.class);
        mapa.put(Campo.EDAD, r.edad() != null ? r.edad().toString() : null);
        mapa.put(Campo.PAIS_REGION, r.paisRegion());
        mapa.put(Campo.POSICION, r.posicion());
        mapa.put(Campo.LIGA, r.liga());
        mapa.put(Campo.EQUIPO_ACTUAL, r.equipoActual());
        return mapa;
    }
}
