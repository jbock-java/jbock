package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h3>Marker for positional parameter methods</h3>
 *
 * <ul>
 * <li>The annotated method must be abstract and have an empty argument list.</li>
 * <li>The annotated method may not carry the {@link Parameter} annotation.</li>
 * </ul>
 *
 * <p>For example, the following shell commands are all using positional parameters:</p>
 * <pre>{@code
 * cd ..
 * git log
 * echo "Hello world!"
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface PositionalParameter {

  /**
   * <p>The parameter position in the sequence of all positional parameters.
   * Lower order parameters come first.</p>
   *
   * <ul>
   * <li>Two parameters must have different position if they are either both required or
   * both optional.</li>
   * <li>Gaps in the position numbers are allowed. Negative numbers are also allowed.</li>
   * <li>Required parameters must have the lowest positions.</li>
   * <li>There can only be one {@link #repeatable()} positional parameter, and it must have the highest position.</li>
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
   * @see Parameter#repeatable
   *
   * @return a boolean
   */
  boolean repeatable() default false;

  /**
   * @see Parameter#optional
   *
   * @return a boolean
   */
  boolean optional() default false;

  /**
   * @see Parameter#bundleKey
   *
   * @return a string
   */
  String bundleKey() default "";
}

