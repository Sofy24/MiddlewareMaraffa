package org.example.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.utils.Constants;

import java.util.Objects;

public class IsRoundEndedResponse {
    @JsonProperty(Constants.ENDED)
    private Boolean isEnded;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IsRoundEndedResponse)) return false;
        IsRoundEndedResponse that = (IsRoundEndedResponse) o;
        return isEnded == that.isEnded;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEnded);
    }
}
