package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for a name option.
 * The annotated method must be {@code abstract}
 * and have an empty argument list.
 * The method's enclosing class must carry the {@link Command}
 * or {@link SuperCommand} annotation.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Option {

  /**
   * The names of this option.
   * A name can be either a gnu name, prefixed with two dashes,
   * or a unix name consisting of single dash, followed by
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
   * or a {@link java.util.function.Supplier Supplier} thereof.
   * It must either be a {@code static} inner class of the class carrying the {@link Command} annotation,
   * or, if it is declared in a separate source file, it must carry the {@link Converter} annotation.
   *
   * @return an optional mapper class, or {@code Void.class} to represent "none"
   */
  Class<?> converter() default Void.class;

  /**
   * The key that is used to find the parameter
   * description in the i18 resource bundle for the online help.
   * If no bundleKey is defined,
   * or no bundle is supplied at runtime,
   * or a bundle is supplied but does not contain the bundle key,
   * then the {@code abstract} method's javadoc is used as description.
   *
   * @return bundle key or empty string
   */
  String bundleKey() default "";
}
