package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.utils.Constants;

public class GetGamesResponse {
	@JsonProperty(Constants.GAME)
	private String[] game;
}
