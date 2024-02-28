package org.example.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.utils.Constants;

import java.util.UUID;

public class GetGamesResponse {
    @JsonProperty(Constants.USERNAME)
    private String[] game;
}
