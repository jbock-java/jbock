package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * Non-exceptional failure object that indicates an error in this command's
 * command line syntax.
 *
 * @see net.jbock.Command
 */
public final class ErrToken extends NotSuccess implements HasMessage {

    private final ErrTokenType errorType;
    private final String token;

    /**
     * Public constructor.
     *
     * @param commandModel command model
     * @param errorType specific error type
     * @param token offending token
     */
    public ErrToken(CommandModel commandModel, ErrTokenType errorType, String token) {
        super(commandModel);
        this.errorType = errorType;
        this.token = token;
    }

    /**
     * Returns the error type.
     *
     * @return the error type
     */
    public ErrTokenType errorType() {
        return errorType;
    }

    /**
     * Returns the item related to this error.
     *
     * @return the item name
     */
    public String token() {
        return token;
    }

    @Override
    public String message() {
        switch (errorType) {
            case MISSING_ARGUMENT:
                return "Missing argument after option name: " + token;
            case INVALID_OPTION:
                return "Invalid option: " + token;
            case EXCESS_PARAM:
                return "Excess param: " + token;
            case OPTION_REPETITION:
                return String.format("Option '%s' is a repetition", token);
            case INVALID_UNIX_GROUP:
                return "Invalid token: " + token;
            default:
                throw new AssertionError("all cases exhausted");
        }
    }
}
