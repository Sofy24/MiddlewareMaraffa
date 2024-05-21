package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.utils.Constants;

import java.util.Objects;

public class CardsOnHandResponse {
    @JsonProperty(Constants.TRICK)
    private String trick;

}
