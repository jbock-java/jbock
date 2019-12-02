package net.jbock.examples;

import net.jbock.CLI;
import net.jbock.Option;
import net.jbock.Param;

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
@CLI(programName = "curl")
abstract class CurlArguments {

  /**
   * Optional<String> for regular arguments
   */
  @Option(value = "request", mnemonic = 'X')
  abstract Optional<String> method();

  /**
   * List<String> for repeatable arguments
   */
  @Option(value = "H", mnemonic = 'H')
  abstract List<String> headers();

  /**
   * boolean for flags
   */
  @Option(value = "verbose", mnemonic = 'v')
  abstract boolean verbose();

  @Option(value = "include", mnemonic = 'i')
  abstract boolean include();

  @Param(value = 1)
  abstract List<String> urls();
}
