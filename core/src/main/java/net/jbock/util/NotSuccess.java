package net.jbock.util;

/**
 * An implementation of this interface represents any parsing
 * result that's different from parsing success.
 * There are a fixed number of implementations:
 *
 * <ul>
 *   <li>{@link Failure}</li>
 *   <li>{@link HelpRequested}</li>
 * </ul>
 *
 * This interface will be a sealed interface in later versions.
 */
public interface NotSuccess {
}
