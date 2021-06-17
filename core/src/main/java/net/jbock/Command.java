package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for an {@code abstract} class that is used
 * to define a command line API.
 * Each of its {@code abstract} methods must be
 * either an {@link Option @Option} or a {@link Parameter @Parameter},
 * or carry the {@link Parameters @Parameters} annotation.
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface Command {

  /**
   * The program name used in the usage documentation.
   * If empty, a program name will be chosen based on the
   * class name of the annotated class.
   *
   * @return program name, or empty string
   */
  String name() default "";

  /**
   * If {@code true},
   * the generated parser will print the usage documentation
   * when {@code --help} or {@code -h}
   * are the only input tokens, or when there is at least one
   * required option or parameter, and the input array is empty.
   * If {@code false}, the usage documentation will be printed
   * when there is a parsing error.
   *
   * @return {@code false} to disable the help option
   */
  boolean helpEnabled() default true;

  /**
   * Optional text to display before the synopsis block, in the usage documentation.
   * If empty, the javadoc of the annotated class will be used as a fallback.
   * If {@code descriptionKey} is not empty, an attempt will be made
   * to read the description from the message map first.
   *
   * @return description text
   */
  String[] description() default {};

  /**
   * The key that is used to find the command description
   * in the internationalization message map.
   * If no {@code descriptionKey} is defined,
   * or the runtime message map does not contain the description key,
   * then the {@code description} attribute will be used.
   * If that is also empty, the javadoc of the Command class will be used
   * as a fallback.
   *
   * @return key or empty string
   */
  String descriptionKey() default "";

  /**
   * Enables or disables ANSI colors in the usage documentation.
   * By default, colors and bold text are used to highlight
   * certain keywords.
   *
   * @return {@code false} to disable ANSI colors
   */
  boolean ansi() default true;

  /**
   * <p>Enables or disables the so-called {@code @file} (read: &quot;at-file&quot;) expansion.
   * If the first token in the input array starts with an {@code @} character
   * and is at least 2 characters long,
   * then this token is interpreted as the name of an options-file,
   * containing lines of {@code UTF-8} encoded tokens.
   * Trailing empty lines are ignored.
   * <p>The following escape sequences are recognized:</p>
   *
   * <br/>
   * <table>
   *   <caption>Escape sequences</caption>
   *   <thead><tr><td><b>Code</b></td><td><b>Meaning</b></td></tr></thead>
   *   <tr><td>{@code \\}</td><td>backslash</td></tr>
   *   <tr><td>{@code \n}</td><td>newline</td></tr>
   *   <tr><td>{@code \r}</td><td>carriage return</td></tr>
   *   <tr><td>{@code \t}</td><td>horizontal tab</td></tr>
   * </table>
   *
   * <p>An unpaired backslash at the end of a line prevents
   * the newline from being read.</p>
   * <p>Note: Even if set to {@code true},
   * and the user wants to pass exactly one positional parameter
   * that starts with an {@code @} character,
   * they can still prevent the {@code @file} expansion,
   * by passing {@code --} as the first token.</p>
   *
   * @return {@code false} to disable the {@code @file} expansion
   */
  boolean atFileExpansion() default true;

  /**
   * <p>If {@code true}, the generated parser will stop parsing after the
   * last positional parameter was read,
   * and return the remaining tokens as an array of strings.
   * The double-dash is not recognized as a special token.</p>
   *
   * <p>The following additional rules apply, then:</p>
   *
   * <ul>
   *   <li>There must be at least one {@link Parameter @Parameter}.</li>
   *   <li>{@link Parameters @Parameters} cannot be used.</li>
   * </ul>
   *
   * @return {@code true} to make this a SuperCommand
   */
  boolean superCommand() default false;

  /**
   * Disables clustering of unix short options.
   * In order for this to do have any effect, there must
   * be at least one nullary unix-style option,
   * and one other option with a unix name,
   * possibly also nullary.
   *
   * @return {@code false} to disable clustering
   */
  boolean unixClustering() default true;
}
