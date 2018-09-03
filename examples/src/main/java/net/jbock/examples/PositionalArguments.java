package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Positional;

import java.util.Optional;

@CommandLineArguments(allowEscape = true)
abstract class PositionalArguments {

  @Positional
  abstract String source();

  @Positional
  abstract String dest();

  @Positional
  abstract Optional<String> optString();

  @Positional
  abstract String[] otherTokens();
}
