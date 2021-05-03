package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for an {@code abstract} class that is used
 * to define a command line API.
 * Each of its {@code abstract} methods must have an empty argument list and must be
 * annotated with either {@link Option} or {@link Param}.
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface Command {

  /**
   * The name that the program will be addressed by when the full
   * usage information is printed.
   * If an empty string is used, the name will be based on the
   * name of the annotated class.
   *
   * @return the intended file name of the final executable
   */
  String value() default "";

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
