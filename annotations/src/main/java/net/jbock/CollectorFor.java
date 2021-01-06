package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * The annotated method must have an empty argument list
 * and may not be {@code abstract}.
 * It must return an instance of {@link java.util.stream.Collector}.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface CollectorFor {

  /**
   * The exact name of the abstract method that defines the
   * repeatable positional parameter or named option
   * for which this collector should be registered.
   */
  String value();
}
