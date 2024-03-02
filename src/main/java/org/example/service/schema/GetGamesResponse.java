package org.example.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.utils.Constants;


public class GetGamesResponse {
    @JsonProperty(Constants.GAME)
    private String[] game;

}
