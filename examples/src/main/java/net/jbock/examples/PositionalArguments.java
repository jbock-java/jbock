package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;

@CommandLineArguments(allowEscapeSequence = true)
abstract class PositionalArguments {

  @PositionalParameter(position = -10)
  abstract String source();

  /**
   * Desc of dest.
   */
  @PositionalParameter(position = -1, descriptionArgumentName = "DESTINATION")
  abstract String dest();

  @PositionalParameter(position = 2)
  abstract int anotherInt();

  @PositionalParameter(position = 3)
  abstract Optional<String> optString();

  @PositionalParameter(position = 4)
  abstract List<String> otherTokens();
}
