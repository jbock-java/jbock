package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;

@CommandLineArguments
abstract class RestArguments {

  /**
   * This is the file.
   */
  @Parameter(value = "file", bundleKey = "param_file")
  abstract List<String> file();

  @PositionalParameter(value = 1)
  abstract List<String> rest();
}
