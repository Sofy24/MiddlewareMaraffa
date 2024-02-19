package org.example.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.utils.Constants;

public class MakeCallBody {
    @JsonProperty(Constants.GAME_ID)
    private String gameID;
    @JsonProperty(Constants.CALL)
    private String call;
    @JsonProperty(Constants.USERNAME)
    private String username;
}
