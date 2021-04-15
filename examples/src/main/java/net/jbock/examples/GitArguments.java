package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Param;

import java.util.List;

@Command
abstract class GitArguments {

  @Param(0)
  abstract String command();

  @Param(1)
  abstract List<String> remainingArgs();
}
