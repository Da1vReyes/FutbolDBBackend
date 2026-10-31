package edu.uam.futboldb.game;

import edu.uam.futboldb.player.Player;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.EnumSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Player jugadorSeleccionado;

    /** Email del usuario que creó la partida; solo él puede responderla. */
    @Column(nullable = false)
    private String usuarioEmail;

    private int intentosRealizados = 0;

    private boolean finalizada;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Campo> camposAcertados = EnumSet.noneOf(Campo.class);
}
