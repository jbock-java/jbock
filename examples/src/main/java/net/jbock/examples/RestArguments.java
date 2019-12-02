package net.jbock.examples;

import net.jbock.CLI;
import net.jbock.Option;
import net.jbock.Param;

import java.util.List;

@CLI
abstract class RestArguments {

  /**
   * This is the file.
   */
  @Option(value = "file", bundleKey = "param_file")
  abstract List<String> file();

  @Param(value = 1)
  abstract List<String> rest();
}
