package net.jbock.examples;


import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

@CommandLineArguments
abstract class TarArguments {

  @Parameter(flag = true, shortName = 'x')
  abstract boolean extract();

  @Parameter(flag = true, shortName = 'c')
  abstract boolean create();

  @Parameter(flag = true, shortName = 'v')
  abstract boolean verbose();

  @Parameter(flag = true, shortName = 'z')
  abstract boolean compress();

  @Parameter(shortName = 'f')
  abstract String file();
}
