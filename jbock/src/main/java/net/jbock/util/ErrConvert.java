package net.jbock.util;

import net.jbock.model.CommandModel;
import net.jbock.model.Item;

/**
 * Represents a runtime error during a converter invocation.
 */
public record ErrConvert(
        CommandModel commandModel,
        String message,
        Item item) implements ParsingFailed {
}
