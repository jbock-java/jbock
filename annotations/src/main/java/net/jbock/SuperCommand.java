package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * <p>Marker annotation for an {@code abstract} class that is used
 * to define a command line API.
 * The generated parser will stop parsing after the last
 * positional parameter was read,
 * and return the remaining tokens as an array of strings.
 * The double-dash is not recognized as a special token.</p>
 *
 * <ul>
 *   <li>Each of the {@code abstract} methods must be either an {@link Option @Option}
 *   or a {@link Parameter @Parameter}</li>
 *   <li>There must be at least one {@link Parameter @Parameter}.</li>
 *   <li>{@link Parameters @Parameters} cannot be used in a {@link SuperCommand @SuperCommand}.</li>
 * </ul>
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface SuperCommand {

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
   * If that is also empty, the javadoc of the SuperCommand class will be used
   * as a fallback.
   *
   * @return key or empty string
   */
  String descriptionKey() default "";

  /**
   * Enables or disable ANSI colors in the usage documentation.
   * By default, colors and bold text are used to highlight
   * certain keywords.
   * 
   * @return {@code false} to disable ANSI colors
   */
  boolean ansi() default true;
}
