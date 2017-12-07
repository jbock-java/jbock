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
   *   If enabled, a double hyphen &quot;{@code --}&quot; ends option parsing, when encountered
   *   as an unbound token.
   * </p>
   * <p>
   *   When option parsing ends, the double hyphen itself along with all remaining tokens
   *   after it,
   *   are added to the list of positional arguments.
   * </p>
   */
  boolean esc() default true;
}
