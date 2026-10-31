package edu.uam.futboldb.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uam.futboldb.player.Player;
import edu.uam.futboldb.player.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Flujo completo contra H2: registro, login, nueva partida y respuesta. */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PlayerRepository playerRepository;

    private String enviar(String url, String token, String body, int status) throws Exception {
        var req = post(url).contentType(MediaType.APPLICATION_JSON).content(body);
        if (token != null) req.header("Authorization", "Bearer " + token);
        MvcResult res = mvc.perform(req).andExpect(status().is(status)).andReturn();
        return res.getResponse().getContentAsString();
    }

    @Test
    void registroGuardaHashYNoDevuelveLaContrasena() throws Exception {
        String body = enviar("/api/users/register", null,
                "{\"name\":\"Ana\",\"email\":\"Hash@Example.com\",\"password\":\"secreto123\"}", 201);

        assertFalse(body.contains("password"));
        assertFalse(body.contains("secreto123"));
        String guardado = userRepository.findByEmail("hash@example.com").orElseThrow().getPassword();
        assertTrue(guardado.startsWith("$2"), "debe ser un hash BCrypt");
    }

    @Test
    void registroRechazaDatosInvalidosYEmailsRepetidos() throws Exception {
        enviar("/api/users/register", null, "{\"name\":\"\",\"email\":\"no-es-email\",\"password\":\"corta\"}", 400);
        String ok = "{\"name\":\"Bea\",\"email\":\"bea@example.com\",\"password\":\"secreto123\"}";
        enviar("/api/users/register", null, ok, 201);
        enviar("/api/users/register", null, ok, 409);
    }

    @Test
    void loginConCredencialesMalasDa401() throws Exception {
        enviar("/api/users/register", null, "{\"name\":\"Cris\",\"email\":\"cris@example.com\",\"password\":\"secreto123\"}", 201);
        enviar("/api/auth/login", null, "{\"email\":\"cris@example.com\",\"password\":\"otra-cosa\"}", 401);
        enviar("/api/auth/login", null, "{\"email\":\"nadie@example.com\",\"password\":\"secreto123\"}", 401);
    }

    @Test
    void rutasProtegidasExigenToken() throws Exception {
        mvc.perform(post("/api/game/nueva")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer token-falso")).andExpect(status().isUnauthorized());
    }

    @Test
    void flujoCompletoRegistroLoginPartidaYRespuesta() throws Exception {
        playerRepository.save(Player.builder().playerName("Jugador Test").edad(30).paisRegion("Chile")
                .posicion("POR").liga("Liga Test").equipoActual("Club Test").build());

        enviar("/api/users/register", null, "{\"name\":\"Dani\",\"email\":\"dani@example.com\",\"password\":\"secreto123\"}", 201);
        JsonNode login = json.readTree(enviar("/api/auth/login", null, "{\"email\":\"dani@example.com\",\"password\":\"secreto123\"}", 200));
        String token = login.get("token").asText();

        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("dani@example.com"));

        JsonNode partida = json.readTree(enviar("/api/game/nueva", token, "", 201));
        long gameId = partida.get("gameId").asLong();
        assertEquals(2, partida.get("pistas").size());

        String respuesta = "{\"edad\":30,\"paisRegion\":\"Chile\",\"posicion\":\"POR\",\"liga\":\"Liga Test\",\"equipoActual\":\"Club Test\"}";
        JsonNode resultado = json.readTree(enviar("/api/game/" + gameId + "/responder", token, respuesta, 200));
        assertTrue(resultado.get("finalizada").asBoolean());
        assertEquals("Jugador Test", resultado.get("jugador").asText());

        // Otro usuario no puede tocar esta partida
        enviar("/api/users/register", null, "{\"name\":\"Eli\",\"email\":\"eli@example.com\",\"password\":\"secreto123\"}", 201);
        String tokenEli = json.readTree(enviar("/api/auth/login", null, "{\"email\":\"eli@example.com\",\"password\":\"secreto123\"}", 200)).get("token").asText();
        enviar("/api/game/" + gameId + "/responder", tokenEli, respuesta, 404);
    }
}
