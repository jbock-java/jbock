package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Param;

import java.util.List;
import java.util.Optional;

@Command
abstract class PositionalArguments {

  @Param(-10)
  abstract String source();

  /**
   * Desc of dest.
   */
  @Param(-1)
  abstract String dest();

  @Param(2)
  abstract int anotherInt();

  @Param(3)
  abstract Optional<String> optString();

  @Param(4)
  abstract List<String> otherTokens();
}
