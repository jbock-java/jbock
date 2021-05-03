package net.jbock.examples;


import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Param;

import java.util.List;

@Command
abstract class RequiredArguments {

  @Option(names = "--dir")
  abstract String dir();

  @Param(0)
  abstract List<String> otherTokens();
}
