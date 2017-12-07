package net.jbock.examples;


import java.util.List;
import net.jbock.CommandLineArguments;
import net.jbock.Positional;

@CommandLineArguments(allowGrouping = true)
abstract class RequiredArguments {

  abstract String dir();

  @Positional
  abstract List<String> otherTokens();
}
