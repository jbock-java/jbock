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
 *   The annotated type must be an abstract class.
 *   Each abstract method in the annotated class must have an empty argument list.
 * </p>
 *
 * @see <a href="https://github.com/h908714124/jbock">jbock on github</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CommandLineArguments {

  /**
   * <p>
   *   {@code true} if unknown tokens should be read as positional arguments (if any are declared),
   *   even if they start with the hyphen character.
   * </p><p>
   *   If {@code false}, an unknown token that starts with a hyphen
   *   (and is not bound to an option name) will raise an {@link IllegalArgumentException}.
   * </p>
   */
  boolean ignoreDashes() default false;
}
