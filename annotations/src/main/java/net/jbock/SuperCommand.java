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
 * <p>Each of the {@code abstract}
 * methods must have an empty argument list, and must be
 * annotated with either {@link Option} or {@link Parameter},
 * but not {@link Parameters}.
 * There must be at least one method with a {@link Parameter} annotation.
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
   *
   * @return {@code false} to disable the help option
   */
  boolean helpEnabled() default true;

  /**
   * Optional text to display before the synopsis block, in the usage documentation.
   * If empty, the javadoc of the annotated class will be used as a fallback.
   *
   * @return description text
   */
  String[] description() default {};

  /**
   * The key that is used to find the command description
   * in the internationalization message map.
   * If no {@code descriptionKey} is defined,
   * or no message map is supplied at runtime,
   * or a message map is supplied but does not contain the description key,
   * then the {@code description} attribute will be used.
   * If that is also empty, the class-level javadoc will be used.
   *
   * @return key or empty string
   */
  String descriptionKey() default "";
}
