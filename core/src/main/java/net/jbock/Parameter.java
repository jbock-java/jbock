package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>This annotation is used by the jbock annotation processor.</p>
 *
 * <ul>
 * <li>The annotated method must be abstract.</li>
 * <li>The annotated method must have an empty argument list.</li>
 * <li>The annotated method must not also carry the {@link PositionalParameter} annotation.</li>
 * <li>The enclosing class must be annotated with {@link CommandLineArguments}.</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Parameter {

  /**
   * <p>Long names define 'gnu style' arguments that start with two dashes.</p>
   *
   * <p>This string must not contain whitespace or the equals '=' character,
   * or start with the dash '-'.
   * A single dash however is special syntax for automatic name.
   * An empty string indicates that this parameter doesn't define a long name.</p>
   *
   * @return either a single dash, an empty string or a valid long name
   */
  String longName() default "-";

  /**
   * <p>Short names define arguments that start with a single dash followed
   * by a single character. It is not possible to define a short name with more than
   * one character.</p>
   *
   * <p>A space is special syntax for no short name.</p>
   *
   * @return either space or a character that is not a dash
   */
  char shortName() default ' ';
}
