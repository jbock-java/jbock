package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h2>Marks a class as having parameter methods</h2>
 *
 * <ul>
 * <li>The annotated class must be abstract.</li>
 * <li>The annotated class must be simple: It cannot have type parameters.
 *  It also cannot implement any interfaces or extend any other class.</li>
 * <li>Every abstract method must be annotated with {@link Parameter} or {@link PositionalParameter}.</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CommandLineArguments {

  /**
   * <p>The name of the final executable program.
   * If the java program is usually invoked from a wrapper script,
   * then this should be the file name of that script.</p>
   *
   * <p>The program name is printed when the user passes the
   * {@code --help} parameter, in the {@code NAME} section of the usage information.
   * By default, the short name of the annotated java class is used as the program name.
   * If that class is an inner class,
   * then the short name of its enclosing class is the default program name</p>
   *
   * @return an optional program name
   */
  String programName() default "";

  /**
   * A short, single-sentence summary of the program. It is printed when the user passes the
   * {@code --help} parameter.
   *
   * @return an optional string
   */
  String missionStatement() default "";

  /**
   * <p>If {@code true}, a special parameter {@code --help} will be understood by the parser,
   * but only if it is the very first argument.
   * In this case, it is an error to assign the long name "help" to any other parameter.</p>
   *
   * @return false to disable the standard {@code --help} functionality
   */
  boolean addHelp() default true;

  /**
   * <p>True if an isolated double dash "--" should end option parsing.
   * The remaining tokens will then be treated as positional, regardless of their shape.</p>
   *
   * @return false to disable the standard <em>end of option parsing</em> escape sequence
   */
  boolean allowEscape() default true;

  /**
   * <p>True if unknown tokens that start with a dash should be permissible.
   * These tokens will then be treated as positional.
   * Otherwise these tokens are treated as bad input, and parsing fails.</p>
   *
   * <p>Note that <em>any</em> unknown token is considered bad input,
   * if no positional parameters are defined. See {@link PositionalParameter}.</p>
   *
   * <p>Note that setting {@link #allowEscape()} (and defining a positional list) makes the double dash "--" a known token.</p>
   *
   * @return false if tokens that start with a dash should be allowed as positional parameters
   */
  boolean strict() default true;
}
