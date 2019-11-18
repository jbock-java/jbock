package net.jbock.examples;


import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

@CommandLineArguments
abstract class TarArguments {

  @Parameter(value = "x", mnemonic = 'x')
  abstract boolean extract();

  @Parameter(value = "c", mnemonic = 'c')
  abstract boolean create();

  @Parameter(value = "v", mnemonic = 'v')
  abstract boolean verbose();

  @Parameter(value = "z", mnemonic = 'z')
  abstract boolean compress();

  @Parameter(value = "f", mnemonic = 'f')
  abstract String file();
}
