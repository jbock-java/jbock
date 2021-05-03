package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for a positional parameter.
 * The annotated method must be {@code abstract}
 * and have an empty argument list.
 * The method's enclosing class must carry the {@link Command}
 * or {@link SuperCommand} annotation.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Param {

  /**
   * This parameter's position.
   * The first parameter's position is {@code 0}.
   *
   * @return the param's position
   */
  int value();

  /**
   * Declare a custom converter for this positional parameter.
   * This is either a
   * {@link java.util.function.Function Function}
   * accepting strings,
   * or a {@link java.util.function.Supplier Supplier} thereof.
   * It must either be a {@code static} inner class of the class carrying the {@link Command} annotation,
   * or, if it is declared in a separate source file, it must carry the {@link Converter} annotation.
   *
   * @return a mapper class, or {@code Void.class}
   */
  Class<?> converter() default Void.class;

  /**
   * The key that is used to look up the parameter
   * description in the internationalization resource bundle for the online help.
   * If no such key is defined,
   * or no bundle is supplied at runtime,
   * or the bundle supplied at runtime does not contain the bundle key,
   * then the {@code abstract} method's javadoc is used as the param's description.
   *
   * @return bundle key or empty string
   */
  String bundleKey() default "";
}

