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
   *   All tokens in the option group must be flags in their short form.
   *   Binding arguments may not appear in the option group, not even in the last position.
   * </p>
   *
   * <p>
   *   The first flag in the group must be preceded with a hyphen.
   *   It is not permissible to omit this leading hyphen.
   * </p>
   *
   * <p>
   *   For example, in {@code tar xzf foobar.tgz}, the first token {@code xzf}
   *   would be an option group, consisting of three flags {@code x}, {@code z}
   *   and {@code f}. Note that {@code f} must be a flag, too.
   *   Therefore, {@code foobar.tgz} is a positional argument,
   *   not an argument bound to {@code f}.
   * </p>
   */
  boolean grouping() default false;
}
