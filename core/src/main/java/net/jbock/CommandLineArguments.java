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
 * <ul>
 *   <li>The annotated type must be an abstract class.</li>
 *   <li>There must be at least one abstract method.</li>
 *   <li>Each abstract method must have an empty argument list.</li>
 *   <li>The class may not extend or implement anything, other than {@link java.lang.Object}.</li>
 * </ul>
 *
 * <p>Each abstract method in the annotated class must return one of these types:</p>
 *
 * <ul>
 *   <li>{@code boolean}</li>
 *   <li>{@code String}</li>
 *   <li>{@code Optional<String>}</li>
 *   <li>{@code List<String>}</li>
 *   <li>{@code int}</li>
 *   <li>{@code OptionalInt}</li>
 * </ul>
 *
 * @see <a href="https://github.com/h908714124/jbock">jbock on github</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CommandLineArguments {

  /**
   * <p>
   *   Should unknown tokens should always be interpreted as positional arguments,
   *   even if they start with a hyphen character?
   * </p><p>
   *   If {@code false}, an unknown unbound token that starts with a hyphen
   *   will always raise an {@link IllegalArgumentException}.
   * </p>
   */
  boolean ignoreDashes() default false;

  String[] description() default {};
}
