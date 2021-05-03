package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;
import net.jbock.Parameters;

import java.util.List;

@Command
abstract class GitArguments {

  @Parameter(index = 0)
  abstract String command();

  @Parameters
  abstract List<String> remainingArgs();
}
