package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;

/**
 * curl  is  a  tool  to  transfer data from or to a server
 * using one of the supported protocols.
 * <p>
 * curl offers a busload of useful tricks.
 * <p>
 * curl is powered by libcurl for all transfer-related features.
 * See libcurl(3) for details.
 */
@CommandLineArguments(programName = "curl")
abstract class CurlArguments {

  /**
   * Optional<String> for regular arguments
   */
  @Parameter(value = "request", mnemonic = 'X')
  abstract Optional<String> method();

  /**
   * List<String> for repeatable arguments
   */
  @Parameter(value = "H", mnemonic = 'H')
  abstract List<String> headers();

  /**
   * boolean for flags
   */
  @Parameter(value = "verbose", mnemonic = 'v')
  abstract boolean verbose();

  @Parameter(value = "include", mnemonic = 'i')
  abstract boolean include();

  @PositionalParameter(value = 1)
  abstract List<String> urls();
}
