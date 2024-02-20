package org.example.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.utils.Constants;

import java.util.Objects;

public class isRoundEnded {
    @JsonProperty(Constants.ENDED)
    private Boolean isEnded;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof isRoundEnded)) return false;
        isRoundEnded that = (isRoundEnded) o;
        return isEnded == that.isEnded;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEnded);
    }
}
