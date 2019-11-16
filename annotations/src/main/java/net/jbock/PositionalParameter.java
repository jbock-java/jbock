package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for positional parameters.
 * The annotated method must be abstract and have an empty argument list.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface PositionalParameter {

  /**
   * <p>The parameter position in the sequence of all positional parameters.
   * The lowest position defines the first positional parameter.</p>
   *
   * <ul>
   * <li>Gaps and negative numbers are also allowed.</li>
   * <li>Required parameters must have the lowest positions.</li>
   * <li>There can only be one repeatable positional parameter,
   * and it must have the highest position.</li>
   * </ul>
   *
   * @return parameter position
   */
  int position() default 0;

  /**
   * @see Parameter#descriptionArgumentName
   *
   * @return a string
   */
  String descriptionArgumentName() default "";

  /**
   * @see Parameter#mappedBy
   *
   * @return a class
   */
  Class<?> mappedBy() default Object.class;

  /**
   * @see Parameter#collectedBy
   *
   * @return a class
   */
  Class<?> collectedBy() default Object.class;

  /**
   * @see Parameter#bundleKey
   *
   * @return a string
   */
  String bundleKey() default "";
}

