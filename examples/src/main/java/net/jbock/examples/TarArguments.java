package net.jbock.examples;


import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

@CommandLineArguments
abstract class TarArguments {

  @Parameter(shortName = 'x')
  abstract boolean extract();

  @Parameter(shortName = 'c')
  abstract boolean create();

  @Parameter(shortName = 'v')
  abstract boolean verbose();

  @Parameter(shortName = 'z')
  abstract boolean compress();

  @Parameter(shortName = 'f')
  abstract String file();
}
