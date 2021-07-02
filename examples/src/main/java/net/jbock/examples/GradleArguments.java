package net.jbock.examples;


import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.either.Optional;

import java.util.List;

@Command(unixClustering = true)
abstract class GradleArguments {

  /**
   * the message
   * message goes here
   */
  @Option(names = {"--message", "-m"})
  abstract Optional<String> message();

  /**
   * the files
   */
  @Option(names = {"--file", "-f"}, paramLabel = "INPUT_FILE")
  abstract List<String> file();

  /**
   * the dir
   */
  @Option(names = "--dir", paramLabel = "INPUT_DIR")
  abstract Optional<String> dir();

  /**
   * cmos flag
   */
  @Option(names = {"--c", "-c"}, paramLabel = "THIS_IS_IGNORED")
  abstract boolean cmos();

  @Option(names = {"--verbose", "-v"})
  abstract boolean verbose();

  /**
   * some token
   */
  @Parameter(index = 0, paramLabel = "SOME_TOKEN")
  abstract Optional<String> mainToken();

  /**
   * some more tokens
   */
  @Parameters(paramLabel = "moreTokens")
  abstract List<String> otherTokens();

  @Command
  static abstract class Foo {

    @Option(names = "--bar")
    abstract Optional<Integer> bar();
  }

  @Command
  static abstract class Bar {

    @Option(names = "--bar")
    abstract List<String> bar();
  }
}
