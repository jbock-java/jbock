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
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CommandLineArguments {

  /**
   * <p>
   *   Should an attempt be made to read the first token as an option group?
   * </p>
   *
   * <p>
   *   For example, in {@code tar xzf foobar.tgz}, the first token {@code xzf}
   *   is an option group, consisting of two flags {@code x} and {@code z},
   *   followed by the binding token {@code f}.
   * </p>
   */
  boolean grouping() default true;
}
