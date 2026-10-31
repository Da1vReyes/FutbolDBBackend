package edu.uam.futboldb.player.thesportsdb;

import lombok.Data;

import java.util.List;

@Data
public class PlayerApiContainer {
    private List<PlayerApiResponseDTO> player;

}
