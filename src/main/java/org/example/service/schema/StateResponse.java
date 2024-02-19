package org.example.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.utils.Constants;

import java.util.Objects;

public class StateResponse {
    @JsonProperty(Constants.TRICK)
    private String trick;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StateResponse)) return false;
        StateResponse that = (StateResponse) o;
        return trick.equals(that.trick);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trick);
    }
}
