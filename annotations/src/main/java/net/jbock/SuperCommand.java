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
   * The name that the program will be addressed by when the full
   * usage information is printed.
   * If an empty string is used, the name will be based on the
   * name of the annotated class.
   *
   * @return program name, or empty string
   */
  String name() default "";

  /**
   * When {@code false},
   * then the generated parser will print the full usage information
   * if {@code --help} is encountered as the first token in the input array.
   *
   * @return {@code true} to disable the {@code --help} mechanism,
   * {@code false} to enable it.
   */
  boolean helpEnabled() default true;
}
