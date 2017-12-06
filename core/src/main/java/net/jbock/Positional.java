package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *   This annotation is used by the jbock annotation processor.
 * </p>
 *
 * <p>
 *   The annotated method must be abstract and have
 *   an empty argument list.
 * </p>
 *
 * <p>
 *   The enclosing class must be annotated with {@link CommandLineArguments}.
 * </p>
 *
 * @see <a href="https://github.com/h908714124/jbock">jbock on github</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Positional {

  /**
   * <p>
   *   Set to true if a double hyphen &quot;--&quot;, when encountered
   *   as an unbound token, should end option parsing.
   *   When option parsing ends, all remaining tokens after the double hyphen
   *   are then added to the list of positional arguments.
   * </p>
   *
   * <p>
   *   When double dash escaping is enabled,
   *   the double hyphen itself will not be added to the list of positional arguments.
   * </p>
   */
  boolean doubleDashEscaping() default false;
}
