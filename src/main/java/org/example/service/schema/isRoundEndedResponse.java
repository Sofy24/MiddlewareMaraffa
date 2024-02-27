package org.example.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.utils.Constants;

import java.util.Objects;

public class isRoundEndedResponse {
    @JsonProperty(Constants.ENDED)
    private Boolean isEnded;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof isRoundEndedResponse)) return false;
        isRoundEndedResponse that = (isRoundEndedResponse) o;
        return isEnded == that.isEnded;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEnded);
    }
}
