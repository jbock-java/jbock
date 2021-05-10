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
  @Option(names = "--file", descriptionKey = "the.file")
  abstract List<String> file();

  @Parameters(descriptionKey = "the.rest")
  abstract List<String> rest();
}
