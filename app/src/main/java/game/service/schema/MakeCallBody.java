package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.utils.Constants;

public class MakeCallBody {
    @JsonProperty(Constants.GAME_ID)
    private String gameID;
    @JsonProperty(Constants.CALL)
    private String call;
    @JsonProperty(Constants.USERNAME)
    private String username;
}
