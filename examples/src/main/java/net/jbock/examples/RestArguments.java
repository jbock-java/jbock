package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Param;

import java.util.List;

@Command
abstract class RestArguments {

  /**
   * This is the file.
   */
  @Option(value = "file", bundleKey = "the.file")
  abstract List<String> file();

  @Param(value = 0, bundleKey = "the.rest")
  abstract List<String> rest();
}
