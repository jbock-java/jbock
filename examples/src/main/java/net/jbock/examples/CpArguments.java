package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Positional;
import net.jbock.ShortName;

@CommandLineArguments
abstract class CpArguments {

  @ShortName('r')
  abstract boolean recursive();

  @Positional
  abstract String source();

  @Positional
  abstract String dest();
}
