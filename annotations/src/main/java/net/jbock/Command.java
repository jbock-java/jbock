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
   * The program name used in the help text.
   * If an empty string is used, a program name will be chosen based on the
   * class name.
   *
   * @return program name, or empty string
   */
  String name() default "";

  /**
   * When {@code true},
   * the generated parser will show the help text
   * if {@code --help} or {@code -h}
   * are the only options.
   *
   * @return {@code false} to disable the online help
   */
  boolean helpEnabled() default true;
}
