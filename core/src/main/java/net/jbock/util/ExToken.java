package net.jbock.util;

import net.jbock.model.CommandModel;
import net.jbock.model.Item;

/**
 * A checked exception to signal that a parsing error has occurred
 * that is not associated with a particular item.
 * Internal exception that may be thrown and caught
 * in the generated code.
 *
 * @see Item
 */
public final class ExToken extends ExNotSuccess {

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
    public NotSuccess toError(CommandModel model) {
        return new ErrToken(model, errorType, token);
    }
}
