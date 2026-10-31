package edu.uam.futboldb.player.thesportsdb;

import lombok.Data;

@Data
public class PlayerApiResponseDTO {
    private String idPlayer;
    private String idTeam;
    private String strPlayer;
    private String strTeam;
    private String strSport;
    private String strThumb;
    private String strCutout;
    private String strNationality;
    private String dateBorn;
    private String strStatus;
    private String strGender;
    private String strPosition;
    private String relevance;
}
