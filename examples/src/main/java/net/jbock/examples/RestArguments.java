package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Positional;

import java.util.List;

@CommandLineArguments(strict = false, allowEscape = false)
abstract class RestArguments {

  abstract List<String> file();

  @Positional
  abstract List<String> rest();
}
