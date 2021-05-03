package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for an {@code abstract} class that is used
 * to define a command line API.
 * Each of its {@code abstract} methods must have an empty argument list and must be
 * annotated with either {@link Option}, {@link Parameter} or {@link Parameters}.
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface Command {

  /**
   * The name that the program will be addressed by when the full
   * usage information is printed.
   * If an empty string is used, the program name will be based on the
   * name of the annotated class.
   *
   * @return program name, or empty string
   */
  String name() default "";

  /**
   * When {@code true},
   * the generated parser will recognize the {@code --help} or {@code -h}
   * options.
   *
   * @return {@code false} to disable the online help
   */
  boolean helpEnabled() default true;
}
