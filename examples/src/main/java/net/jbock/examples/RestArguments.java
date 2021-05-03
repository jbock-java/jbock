package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameters;

import java.util.List;

@Command
abstract class RestArguments {

  /**
   * This is the file.
   */
  @Option(names = "--file", bundleKey = "the.file")
  abstract List<String> file();

  @Parameters(bundleKey = "the.rest")
  abstract List<String> rest();
}
