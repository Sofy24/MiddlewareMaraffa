package chatModule.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

import game.utils.Constants;

public class ChatMessageBody extends NotificationBody {
    @JsonProperty(Constants.AUTHOR)
    private String author;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.author == null) ? 0 : this.author.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        final ChatMessageBody other = (ChatMessageBody) obj;
        if (this.author == null) {
            if (other.author != null)
                return false;
        } else if (!this.author.equals(other.author))
            return false;
        return true;
    }

}
