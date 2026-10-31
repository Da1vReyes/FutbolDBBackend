package edu.uam.futboldb.game;

import edu.uam.futboldb.exception.ApiException;
import edu.uam.futboldb.game.GameDtos.NuevaPartidaResponse;
import edu.uam.futboldb.game.GameDtos.RespuestaRequest;
import edu.uam.futboldb.game.GameDtos.ResultadoResponse;
import edu.uam.futboldb.player.Player;
import edu.uam.futboldb.player.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    private static final String USUARIO = "ana@example.com";

    @Mock
    PlayerRepository playerRepository;
    @Mock
    GameRepository gameRepository;

    GameService service;
    Player jugador;

    @BeforeEach
    void setUp() {
        service = new GameService(playerRepository, gameRepository, new Random(1));
        jugador = Player.builder().playerId(1L).playerName("Lionel Messi").edad(38)
                .paisRegion("Argentina").posicion("ED").liga("MLS").equipoActual("Inter Miami").build();
    }

    private Game partidaExistente() {
        Game g = new Game();
        g.setId(7L);
        g.setJugadorSeleccionado(jugador);
        g.setUsuarioEmail(USUARIO);
        return g;
    }

    @Test
    void nuevaPartidaDaDosPistasDistintasSinRevelarElNombre() {
        when(playerRepository.findAll()).thenReturn(List.of(jugador));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> {
            Game g = inv.getArgument(0);
            g.setId(7L);
            return g;
        });

        NuevaPartidaResponse r = service.nuevaPartida(USUARIO);

        assertEquals(7L, r.gameId());
        assertEquals(2, r.pistas().size());
        assertNotEquals(r.pistas().get(0).campo(), r.pistas().get(1).campo());
        r.pistas().forEach(p -> assertFalse(p.valor().contains("Messi")));
    }

    @Test
    void nuevaPartidaSinJugadoresDevuelveConflicto() {
        when(playerRepository.findAll()).thenReturn(List.of());
        ApiException e = assertThrows(ApiException.class, () -> service.nuevaPartida(USUARIO));
        assertEquals(409, e.getStatus().value());
    }

    @Test
    void respuestaParcialMarcaAciertosEIncorrectosIgnorandoMayusculas() {
        Game g = partidaExistente();
        when(gameRepository.findById(7L)).thenReturn(Optional.of(g));

        ResultadoResponse r = service.responder(7L, USUARIO,
                new RespuestaRequest(38, "argentina", "DEL", null, null));

        assertFalse(r.finalizada());
        assertEquals(List.of("edad", "paisRegion"), r.acertadosEstaRonda());
        assertEquals(List.of("posicion"), r.incorrectos());
        assertEquals(List.of("posicion", "liga", "equipoActual"), r.faltantes());
        assertNull(r.jugador());
    }

    @Test
    void acertarTodoFinalizaYRevelaElJugador() {
        Game g = partidaExistente();
        when(gameRepository.findById(7L)).thenReturn(Optional.of(g));

        ResultadoResponse r = service.responder(7L, USUARIO,
                new RespuestaRequest(38, "Argentina", "ED", "MLS", " Inter Miami "));

        assertTrue(r.finalizada());
        assertEquals("Lionel Messi", r.jugador());
        assertTrue(r.faltantes().isEmpty());
        assertTrue(g.isFinalizada());
    }

    @Test
    void losAciertosSeAcumulanEntreIntentos() {
        Game g = partidaExistente();
        when(gameRepository.findById(7L)).thenReturn(Optional.of(g));

        service.responder(7L, USUARIO, new RespuestaRequest(38, null, null, null, null));
        ResultadoResponse r = service.responder(7L, USUARIO, new RespuestaRequest(null, "Argentina", null, null, null));

        assertEquals(2, r.intentos());
        assertEquals(List.of("paisRegion"), r.acertadosEstaRonda());
        assertEquals(2, r.valoresAcertados().size());
    }

    @Test
    void noSePuedeResponderUnaPartidaFinalizada() {
        Game g = partidaExistente();
        g.setFinalizada(true);
        when(gameRepository.findById(7L)).thenReturn(Optional.of(g));

        ApiException e = assertThrows(ApiException.class,
                () -> service.responder(7L, USUARIO, new RespuestaRequest(38, null, null, null, null)));
        assertEquals(409, e.getStatus().value());
    }

    @Test
    void partidaDeOtroUsuarioOInexistenteDa404() {
        when(gameRepository.findById(7L)).thenReturn(Optional.of(partidaExistente()));
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());
        RespuestaRequest vacia = new RespuestaRequest(null, null, null, null, null);

        assertEquals(404, assertThrows(ApiException.class, () -> service.responder(7L, "otro@example.com", vacia)).getStatus().value());
        assertEquals(404, assertThrows(ApiException.class, () -> service.responder(99L, USUARIO, vacia)).getStatus().value());
    }
}
