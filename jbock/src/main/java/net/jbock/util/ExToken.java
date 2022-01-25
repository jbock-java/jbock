package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * A checked exception to signal that a parsing error has occurred
 * that is not associated with a particular item.
 * Internal exception that may be thrown and caught
 * in the generated code.
 *
 * <p>This class is internal API and should not be used
 * in client code. It may be removed without warning in future
 * releases.
 */
public final class ExToken extends ExFailure {

    private final ErrTokenType errorType;
    private final String token;

    /**
     * Public constructor.
     *
     * @param errorType error type
     * @param token offending token
     */
    public ExToken(ErrTokenType errorType, String token) {
        this.errorType = errorType;
        this.token = token;
    }

    @Override
    public ParsingFailed toError(CommandModel model) {
        return new ErrToken(model, errorType, token);
    }
}
