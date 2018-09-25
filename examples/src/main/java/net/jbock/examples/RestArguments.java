package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;

@CommandLineArguments(strict = false, allowEscape = false)
abstract class RestArguments {

  @Parameter(repeatable = true)
  abstract List<String> file();

  @PositionalParameter(repeatable = true)
  abstract List<String> rest();
}
