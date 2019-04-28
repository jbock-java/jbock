package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;

/**
 * curl  is  a  tool  to  transfer data from or to a server
 * using one of the supported protocols.
 *
 * curl offers a busload of useful tricks.
 *
 * curl is powered by libcurl for all transfer-related features.
 * See libcurl(3) for details.
 */
@CommandLineArguments(
    programName = "curl",
    missionStatement = "transfer a URL",
    allowEscapeSequence = true)
abstract class CurlArguments {

  /**
   * Optional<String> for regular arguments
   */
  @Parameter(shortName = 'X', longName = "request")
  abstract Optional<String> method();

  /**
   * List<String> for repeatable arguments
   */
  @Parameter(shortName = 'H')
  abstract List<String> headers();

  /**
   * boolean for flags
   */
  @Parameter(shortName = 'v', longName = "verbose")
  abstract boolean verbose();

  @Parameter(shortName = 'i', longName = "include")
  abstract boolean include();

  @PositionalParameter
  abstract List<String> urls();
}
