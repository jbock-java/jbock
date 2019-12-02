package net.jbock.examples;


import net.jbock.CLI;
import net.jbock.Option;

@CLI
abstract class TarArguments {

  @Option(value = "x", mnemonic = 'x')
  abstract boolean extract();

  @Option(value = "c", mnemonic = 'c')
  abstract boolean create();

  @Option(value = "v", mnemonic = 'v')
  abstract boolean verbose();

  @Option(value = "z", mnemonic = 'z')
  abstract boolean compress();

  @Option(value = "f", mnemonic = 'f')
  abstract String file();
}
