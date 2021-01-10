package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for positional parameters.
 * The annotated method must be abstract
 * and have an empty argument list.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Param {

  /**
   * This number determines the parameter's relative position
   * among all positional parameters.
   *
   * <ul>
   * <li>The method's position in the java source file is irrelevant.</li>
   * <li>Negative numbers are allowed.</li>
   * <li>Required parameters must have lower positions than optional or repeatable parameters.</li>
   * <li>There can only be one repeatable {@code Param},
   * and it must have the greatest position.</li>
   * </ul>
   *
   * @return a unique number that determines this parameter's position
   */
  int value();

  /**
   * Declare a custom mapper for this positional parameter.
   * This is either a
   * {@link java.util.function.Function Function}
   * accepting strings,
   * or a {@link java.util.function.Supplier Supplier} thereof.
   * It must carry the {@link Mapper} annotation.
   *
   * @return an optional mapper class
   */
  Class<?> mapper() default Void.class;

  /**
   * The key that is used to find the parameter
   * description in the i18 resource bundle for the online help.
   * If no bundleKey is defined,
   * or no bundle is supplied at runtime,
   * or a bundle is supplied but doesn't contain the bundle key,
   * then the {@code abstract} method's javadoc is used as description.
   *
   * @return an optional bundle key
   */
  String bundleKey() default "";
}

