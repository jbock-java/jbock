package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for a name option.
 * The annotated method must be {@code abstract}
 * and have an empty argument list.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Option {

  /**
   * The unique names of this option.
   * A name can be either a gnu name, prefixed with two dashes,
   * or a unix name. A unix name consists of single dash, followed by
   * a single-character option name.
   *
   * @return list of option names
   */
  String[] names() default {};

  /**
   * Declare a custom converter for this named option.
   * This is either a
   * {@link java.util.function.Function Function}
   * accepting strings,
   * or a {@link java.util.function.Supplier Supplier} of such a function.
   *
   * @return converter class or {@code Void.class}
   */
  Class<?> converter() default Void.class;

  /**
   * The key that is used to find the option
   * description in the internationalization message map.
   * If no {@code descriptionKey} is defined,
   * or the runtime message map does not contain the description key,
   * then the {@code description} attribute will be used.
   * If that is also empty, the method's javadoc will be used.
   *
   * @return key or empty string
   */
  String descriptionKey() default "";

  /**
   * Option description, used when generating the usage documentation.
   * If empty, the method's javadoc will be used as a fallback.
   * If {@code descriptionKey} is not empty, an attempt will be made
   * to read the description from the message map first.
   *
   * @return description text
   */
  String[] description() default {};

  /**
   * <p>A label for the option's argument, to be used in the usage documentation.
   * If empty, a label will be chosen based on the method name.</p>
   * <p>If this option is a mode flag, the label will be ignored.</p>
   *
   * @return a label
   */
  String paramLabel() default "";
}
