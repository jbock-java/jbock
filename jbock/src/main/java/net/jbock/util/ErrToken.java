package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * Represents an error in the command line input that is not related to any particular
 * option or parameter.
 * The specific type of error is returned from the {@link #errorType()} method.
 */
public record ErrToken(
        CommandModel commandModel,
        ErrTokenType errorType,
        String token) implements ParsingFailed {

    @Override
    public String message() {
        return switch (errorType) {
            case MISSING_ARGUMENT -> "Missing argument after option name: " + token;
            case INVALID_OPTION -> "Invalid option: " + token;
            case EXCESS_PARAM -> "Excess param: " + token;
            case OPTION_REPETITION -> "Option '%s' is a repetition".formatted(token);
            case INVALID_UNIX_GROUP -> "Invalid token: " + token;
        };
    }
}
