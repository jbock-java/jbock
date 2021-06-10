package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * Indicates that a parsing error occurred that is not
 * associated with a particular item.
 * Internal exception that may be thrown and caught
 * in the generated code.
 */
public final class ExToken extends ExNotSuccess {

  private final ErrTokenType errorType;
  private final String token;

  public ExToken(ErrTokenType errorType, String token) {
    this.errorType = errorType;
    this.token = token;
  }

  @Override
  public NotSuccess toError(CommandModel model) {
    return new ErrToken(model, errorType, token);
  }
}
