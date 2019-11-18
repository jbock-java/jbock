package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class PositionalArguments {

  @PositionalParameter(-10)
  abstract String source();

  /**
   * Desc of dest.
   */
  @PositionalParameter(-1)
  abstract String dest();

  @PositionalParameter(2)
  abstract int anotherInt();

  @PositionalParameter(3)
  abstract Optional<String> optString();

  @PositionalParameter(4)
  abstract List<String> otherTokens();
}
