package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;

@CommandLineArguments(allowPrefixedTokens = true)
abstract class RestArguments {

  /**
   * This is the file.
   */
  @Parameter(longName = "file", bundleKey = "param_file")
  abstract List<String> file();

  @PositionalParameter
  abstract List<String> rest();
}
