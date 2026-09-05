package at.pcgamingfreaks.exceptions;

/**
 * Thrown when a non-owner requests media data or a tierlist that is not public.
 * Mapped to 404 so the privacy state of other users' resources is not leaked.
 */
public class MediaNotVisibleException extends RuntimeException {
	public MediaNotVisibleException(String username) {
		super("Media of user %s is not visible".formatted(username));
	}
}
