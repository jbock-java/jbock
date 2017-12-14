package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Positional;

@CommandLineArguments
abstract class MvArguments {

  @Positional
  abstract String source();

  @Positional
  abstract String dest();
}
