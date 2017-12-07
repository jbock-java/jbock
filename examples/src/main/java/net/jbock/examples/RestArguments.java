package net.jbock.examples;

import java.util.List;
import net.jbock.CommandLineArguments;
import net.jbock.Positional;

@CommandLineArguments(allowGrouping = true)
abstract class RestArguments {

  abstract List<String> file();

  @Positional
  abstract List<String> rest();
}
