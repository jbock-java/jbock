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
   * <p>The name of the final executable.
   * If the java program is usually invoked from a wrapper script,
   * then this should be the file name of that script.</p>
   *
   * <p>The program name is printed when the user passes the
   * {@code --help} parameter, in the {@code NAME} section of the usage information.
   * By default, the name of the annotated java class is used as the program name.</p>
   *
   * @return an optional program name
   */
  String programName() default "";

  /**
   * A single-sentence summary of the program.
   *
   * @return an optional mission statement
   */
  String missionStatement() default "";

  /**
   * <p>If {@code true}, the special token {@code --help} will be understood by the parser,
   * but only if it is the very first argument.</p>
   *
   * @return false to disable the special meaning of the {@code --help} parameter
   */
  boolean allowHelpOption() default true;

  /**
   * <p>True if the special token {@code --} should end option parsing.
   * The remaining tokens will then be treated as positional, regardless of their shape.</p>
   *
   * @return true to enable the special meaning of the <em>end of option parsing</em> escape sequence
   */
  boolean allowEscapeSequence() default false;

  /**
   * <p>True if unknown tokens that start with a dash should be allowed,
   * and treated as positional.
   * By default, these tokens are rejected.</p>
   *
   * @return true if tokens that start with a dash should be allowed as positional parameters
   */
  boolean allowPrefixedTokens() default false;
}
