package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

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

  /**
   * <p>The argument name that's printed in the description.
   * Has no effect if this is a boolean argument, i.e. a flag.</p>
   *
   * <p>An empty string is special syntax for automatic parameter name,
   * which will be based on the method name.</p>
   */
  String argHandle() default "";

  /**
   * <p>The supplier must yield a mapper Function that maps string to the parameter method's return type.</p>
   * <p>There are two exceptions to this rule:</p>
   * <ol>
   *   <li>if the parameter is optional, then the mapper must return the element type of the Optional</li>
   *   <li>if the parameter is repeatable, then the mapper must return the element type of the List, or more generally,
   *   the input type of the associated collector</li>
   * </ol>
   */
  Class<? extends Supplier> mappedBy() default Supplier.class;

  /**
   * <p>The supplier must yield a {@link java.util.stream.Collector}</p>
   * <p>This only makes sense for repeatable arguments.</p>
   */
  Class<? extends Supplier> collectedBy() default Supplier.class;

  boolean repeatable() default false;

  boolean optional() default false;
}
