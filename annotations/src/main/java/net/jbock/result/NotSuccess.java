package net.jbock.result;

/**
 * An instance of this class represents any parsing result
 * that's different from a parsing success.
 * These are the implementations:
 *
 * <ul>
 *   <li>{@link SyntaxError}</li>
 *   <li>{@link HelpRequested}</li>
 *   <li>{@link ConversionError}</li>
 * </ul>
 *
 * This will be a sealed interface in later versions.
 */
public interface NotSuccess {
}
