package game;

/** An enum with the values of the calls in the game */
public enum Call {
	BUSSO, STRISCIO_LUNGO, STRISCIO_CORTO, VOLO, NONE;

	public static Call fromUppercaseString(final String call) {
		return switch (call) {
			case "BUSSO" -> BUSSO;
			case "STRISCIO_LUNGO" -> STRISCIO_LUNGO;
			case "STRISCIO_CORTO" -> STRISCIO_CORTO;
			case "VOLO" -> VOLO;
			default -> NONE;
		};
	}
}
