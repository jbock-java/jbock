package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for option methods.
 * The annotated method must be abstract
 * and have an empty argument list.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Option {

  /**
   * A unique &quot;gnu style&quot; long name.
   * This definition doesn't contain the double-dash prefix.
   *
   * @return a nonempty string
   */
  String value();

  /**
   * An optional mnemonic (space char ~= absent).
   * This definition doesn't contain the single-dash prefix.
   *
   * @return a mnemonic
   */
  char mnemonic() default ' ';

  /**
   * Declare a custom mapper for this option.
   * This is either a
   * {@link java.util.function.Function Function}
   * accepting strings,
   * or a {@link java.util.function.Supplier Supplier} thereof.
   *
   * @return an optional mapper class
   */
  Class<?> mappedBy() default Object.class;

  /**
   * Declare a custom collector for a <em>repeatable</em> parameter.
   * This is either a {@link java.util.stream.Collector Collector}
   * or a {@link java.util.function.Supplier Supplier} thereof.
   *
   * @return an optional collector class
   */
  Class<?> collectedBy() default Object.class;
}
