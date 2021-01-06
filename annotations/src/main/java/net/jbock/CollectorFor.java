package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * The annotated method must have an empty argument list
 * and may not be {@code abstract}.
 * It must return an instance of {@link java.util.stream.Collector},
 * the output type of which must be the return type of the corresponding
 * {@code abstract} method.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface CollectorFor {

  /**
   * The exact name of the {@code abstract} method that defines the
   * corresponding repeatable positional parameter or named option.
   */
  String value();
}
