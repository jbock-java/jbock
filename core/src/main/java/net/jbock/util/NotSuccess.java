package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * <p>An instance of this class represents any parsing
 * result that's different from parsing success.
 * This will be returned from the generated code,
 * if the parsing is not successful, or if the user has
 * passed the {@code --help} option on the command line.</p>
 *
 * <p>There are a fixed number of subclasses:</p>
 *
 * <ul>
 *   <li>{@link ParsingError}</li>
 *   <li>{@link HelpRequested}</li>
 * </ul>
 */
public abstract class NotSuccess {

  private final CommandModel commandModel;

  public NotSuccess(CommandModel commandModel) {
    this.commandModel = commandModel;
  }

  public CommandModel commandModel() {
    return commandModel;
  }
}
