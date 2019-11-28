package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for parameter methods.
 * The parameter method must be abstract
 * and have an empty argument list.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface PositionalParameter {

  /**
   * The parameter position in the sequence of all positional parameters.
   * The lowest position defines the first positional parameter.
   *
   * <ul>
   * <li>Gaps and negative numbers are allowed.</li>
   * <li>Required parameters must have the lowest positions.</li>
   * <li>There can only be one repeatable positional parameter,
   * and it must have the highest position.</li>
   * </ul>
   *
   * @return a unique number that determines this parameter's position
   */
  int value();

  /**
   * @return a class
   * @see Parameter#mappedBy
   */
  Class<?> mappedBy() default Object.class;

  /**
   * @return a class
   * @see Parameter#collectedBy
   */
  Class<?> collectedBy() default Object.class;

  /**
   * @return a string
   * @see Parameter#bundleKey
   */
  String bundleKey() default "";
}

