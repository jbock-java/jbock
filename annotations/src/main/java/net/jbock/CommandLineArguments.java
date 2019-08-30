package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h2>Marks a class as having parameter methods</h2>
 *
 * <ul>
 * <li>The annotated class must be {@code abstract}.</li>
 * <li>The annotated class must be simple: It cannot have type parameters.
 *  It may not {@code implement} any interfaces or {@code extend} any other class.
 *  It can't be a non-{@code static} inner class.</li>
 * <li>Its {@code abstract} methods represent command line parameters.
 * Each of these must be annotated with either the
 * {@link Parameter} or the {@link PositionalParameter} annotation.</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CommandLineArguments {

  /**
   * <p>The name of the final executable.
   * If the Java program is compiled to native, or invoked from a wrapper script,
   * then this should be the name of the resulting executable.</p>
   *
   * <p>The program name is printed when the user passes the
   * {@code --help} parameter, in the {@code NAME} section of the usage information.
   * By default, the name of the annotated java class is used as the program name.</p>
   *
   * @return an optional program name
   */
  String programName() default "";

  /**
   * A single-sentence summary of the program's most important purpose.
   * This can be internationalized with the bundle key {@code jbock.mission}.
   *
   * @return an optional mission statement
   */
  String missionStatement() default "";

  /**
   * <p>If {@code true}, the special token {@code --help} will be understood by the parser,
   * but only if it is the very first argument in the argument vector.</p>
   *
   * @return false to disable the special meaning of the {@code --help} parameter
   */
  boolean allowHelpOption() default true;

  /**
   * <p>True if an unbound token consisting of two dashes {@code --} should end option parsing.
   * The remaining tokens will then be treated as positional parameters,
   * regardless of their shape.</p>
   *
   * @return true to enable the special meaning of the <em>end of option parsing</em> escape sequence
   */
  boolean allowEscapeSequence() default false;

  /**
   * <p>True if unbound tokens that start with a dash character {@code -} should be allowed
   * as positional parameters.
   * By default, unbound tokens that start with a dash are treated as a parsing failure.</p>
   *
   * @return true if tokens that start with a dash should be allowed as positional parameters
   */
  boolean allowPrefixedTokens() default false;
}
