package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Positional;

@CommandLineArguments
abstract class FewerPositionalArguments {

  @Positional
  abstract String source();

  @Positional
  abstract String dest();
}
