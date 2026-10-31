package edu.uam.futboldb.player;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Player")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long playerId;

    @Column(nullable = false)
    private String playerName;

    @Column(nullable = false)
    private int edad;

    @Column(nullable = false)
    private String paisRegion;

    @Column(nullable = false)
    private String posicion;

    @Column(nullable = false)
    private String liga;

    @Column(nullable = false)
    private String equipoActual;
}
