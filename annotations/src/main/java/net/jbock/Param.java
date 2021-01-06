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
   * <li>Gaps and negative numbers are allowed.</li>
   * <li>Required parameters must have the lowest positions.</li>
   * <li>There can only be one repeatable positional parameter,
   * and it must have the greatest position.</li>
   * </ul>
   *
   * @return a unique number that determines this parameter's position
   */
  int value();

  /**
   * @return a class
   * @see Option#mappedBy
   */
  Class<?> mappedBy() default Object.class;

  /**
   * @return a class
   * @see Option#collectedBy
   */
  Class<?> collectedBy() default Object.class;

  /**
   * The key that is used to find the parameter
   * description in the i18 resource bundle for the online help.
   * If no bundleKey is defined,
   * or if no bundle is supplied at runtime,
   * then an attempt is made to derive the parameter description
   * from the method's javadoc.
   *
   * @return an optional bundle key
   */
  String bundleKey() default "";
}

