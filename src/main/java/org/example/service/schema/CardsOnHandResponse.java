package org.example.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.utils.Constants;

import java.util.Objects;

public class CardsOnHandResponse {
    @JsonProperty(Constants.TRICK)
    private String trick;

}
