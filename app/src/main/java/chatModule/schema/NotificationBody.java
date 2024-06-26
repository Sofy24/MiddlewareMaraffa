package chatModule.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

import game.utils.Constants;

public class NotificationBody {
    @JsonProperty(Constants.GAME_ID)
    private String gameID;
    @JsonProperty(Constants.MESSAGE)
    private String message;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.gameID == null) ? 0 : this.gameID.hashCode());
        result = prime * result + ((this.message == null) ? 0 : this.message.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        final NotificationBody other = (NotificationBody) obj;
        if (this.gameID == null) {
            if (other.gameID != null)
                return false;
        } else if (!this.gameID.equals(other.gameID))
            return false;
        if (this.message == null) {
            if (other.message != null)
                return false;
        } else if (!this.message.equals(other.message))
            return false;
        return true;
    }

}
