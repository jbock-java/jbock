package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class PositionalArguments {

  @PositionalParameter
  abstract String source();

  /**
   * Desc of dest.
   */
  @PositionalParameter(argHandle = "DESTINATION")
  abstract String dest();

  @PositionalParameter
  abstract int anotherInt();

  @PositionalParameter
  abstract Optional<String> optString();

  @PositionalParameter
  abstract List<String> otherTokens();
}
