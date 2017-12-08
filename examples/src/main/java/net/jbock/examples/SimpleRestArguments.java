package net.jbock.examples;

import java.util.List;
import net.jbock.CommandLineArguments;
import net.jbock.Positional;

@CommandLineArguments
abstract class SimpleRestArguments {

  abstract List<String> file();

  @Positional(esc = false)
  abstract List<String> rest();
}
