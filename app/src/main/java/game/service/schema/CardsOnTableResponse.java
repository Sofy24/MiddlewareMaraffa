package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.utils.Constants;

public class CardsOnTableResponse {
        @JsonProperty(Constants.TRICK)
        private String trick;
}
