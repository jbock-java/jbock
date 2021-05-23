package net.jbock.examples;


import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameters;

import java.util.List;

@Command(ansi = false)
abstract class RequiredArguments {

  @Option(names = "--dir")
  abstract String dir();

  @Parameters
  abstract List<String> otherTokens();
}
