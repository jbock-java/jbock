package net.jbock.util;

/**
 * <p>An implementation of this interface represents any parsing
 * result that's different from parsing success.
 * This will be returned from the generated code,
 * if the parsing is not successful, or if the user has
 * passed the {@code --help} option on the command line.</p>
 *
 * <p>There are a fixed number of implementations:</p>
 *
 * <ul>
 *   <li>{@link ParsingError}</li>
 *   <li>{@link HelpRequested}</li>
 * </ul>
 *
 * <p>This interface will be a sealed interface in later versions.</p>
 */
public interface NotSuccess {
}
