package net.jbock.examples;


import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Param;

import java.util.List;

@Command
abstract class RequiredArguments {

  @Option("dir")
  abstract String dir();

  @Param(1)
  abstract List<String> otherTokens();
}
