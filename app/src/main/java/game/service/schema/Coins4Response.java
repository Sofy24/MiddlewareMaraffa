package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.utils.Constants;

public class Coins4Response {
    @JsonProperty(Constants.COINS_4_NAME)
    private String coins4Username;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((coins4Username == null) ? 0 : coins4Username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Coins4Response other = (Coins4Response) obj;
        if (coins4Username == null) {
            if (other.coins4Username != null)
                return false;
        } else if (!coins4Username.equals(other.coins4Username))
            return false;
        return true;
    }

    

}










