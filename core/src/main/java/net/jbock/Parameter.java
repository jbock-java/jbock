package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for a positional parameter.
 * The annotated method must be abstract
 * and have an empty argument list.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Parameter {

  /**
   * The parameter's index among the command's positional parameters.
   * The first parameter's index must be {@code 0}.
   *
   * @return zero-based index
   */
  int index();

  /**
   * Class of a custom converter for this parameter.
   * This is either a class that extends
   * {@link net.jbock.util.StringConverter StringConverter}
   * or a {@link java.util.function.Supplier Supplier} of a string converter.
   *
   * @return converter class or {@code Void.class} to represent &quot;none&quot;
   */
  Class<?> converter() default Void.class;

  /**
   * The key that is used to find the parameter
   * description in an internationalization bundle.
   *
   * @return description key or empty string
   */
  String descriptionKey() default "";

  /**
   * Description text to be used when generating the usage documentation.
   * Can be overridden via {@code descriptionKey}.
   *
   * @return description text
   */
  String[] description() default {};

  /**
   * A label for this parameter, to be used in the usage documentation.
   * If empty, a label will be chosen based on the method name.
   *
   * @return a label
   */
  String paramLabel() default "";
}
