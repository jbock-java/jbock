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
   *   True, if an attempt should be made to read the first token as an option group.
   * </p>
   *
   * <p>
   *   For example, in {@code tar xzf foobar.tgz}, the first token {@code xzf}
   *   would be an option group, consisting of two flags {@code x} and {@code z},
   *   followed by the binding token {@code f}.
   * </p>
   *
   * <p>
   *   All tokens in the option group must be in their short form, without the hyphen.
   *   Only the first option in the group may be preceded with a hyphen.
   *   It is permissible to omit the hyphen for the first option as well.
   * </p>
   *
   * <p>
   *   All tokens in the option group must be flags, except the last one,
   *   which may be a flag or a binding token. If it is a binding token, it
   *   may not have its value attached. Instead, the next token after the option group
   *   will be bound.
   * </p>
   */
  boolean grouping() default false;
}
