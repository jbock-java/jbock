package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;

import java.util.List;

/**
 * This is ignored
 */
@Command(description = "Git is software for tracking changes in any set of files.")
abstract class GitArguments {

  /**
   * Is Javadoc used here?
   */
  @Parameter(index = 0, description = "nope")
  abstract String command();

  /**
   * Some option
   */
  @Option(names = "--bare", description = "bear")
  abstract boolean bare();

  /**
   * More ignored text
   */
  @Parameters(description = {
      "You were a hit!",
      "Everyone loves you, now."})
  abstract List<String> remainingArgs();
}
