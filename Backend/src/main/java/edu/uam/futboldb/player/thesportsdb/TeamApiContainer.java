package edu.uam.futboldb.player.thesportsdb;

import lombok.Data;
import java.util.List;

@Data
public class TeamApiContainer {
    private List<TeamApiResponseDTO> teams;
}
