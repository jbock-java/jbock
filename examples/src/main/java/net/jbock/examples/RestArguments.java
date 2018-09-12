package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;

@CommandLineArguments(strict = false, allowEscape = false)
abstract class RestArguments {

  @Parameter
  abstract List<String> file();

  @PositionalParameter
  abstract List<String> rest();
}
