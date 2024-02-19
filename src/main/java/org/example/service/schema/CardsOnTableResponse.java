package org.example.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.utils.Constants;

public class CardsOnTableResponse {
        @JsonProperty(Constants.TRICK)
        private String trick;
}
