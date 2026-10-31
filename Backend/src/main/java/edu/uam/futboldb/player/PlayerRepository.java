package edu.uam.futboldb.player;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    boolean existsByPlayerNameAndEquipoActual(String playerName, String equipoActual);
}
