package net.jbock.examples;

import net.jbock.Option;
import net.jbock.Param;
import net.jbock.SuperCommand;

@SuperCommand
abstract class SuperArguments {

  @Option(value = "quiet", mnemonic = 'q')
  abstract boolean quiet();

  @Param(0)
  abstract String command();
}
