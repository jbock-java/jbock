package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

/**
 * Demonstrates use of jbock to process command-line
 * arguments in a Java application.
 */
public class Main {

  @Command
  abstract static class Arguments {

    /**
     * Verbosity enabled?
     */
    @Option(value = "verbose", mnemonic = 'v')
    abstract boolean verbose();

    /**
     * File name and path
     */
    @Option(value = "file", mnemonic = 'f')
    abstract String file();
  }

  public static void main(String[] arguments) {
    Arguments args = new Main_Arguments_Parser().parseOrExit(arguments);
    System.out.println("The file '" + args.file() + "' was provided and verbosity is set to '" + args.verbose() + "'.");
  }
}
