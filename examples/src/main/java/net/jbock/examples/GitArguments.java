package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Param;

import java.util.List;

@Command
abstract class GitArguments {

  @Param(value = 1)
  abstract String command();

  @Param(value = 2)
  abstract List<String> remainingArgs();
}
