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
 * <li>The annotated method may not also carry the {@link PositionalParameter} annotation.</li>
 * <li>The method's enclosing class must be annotated with {@link CommandLineArguments}.</li>
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
   * <p>The supplier must yield a {@link java.util.function.Function Function&lt;String, X&gt;}
   * where {@code X} is called the <em>mapper type</em>.
   * In many cases, the mapper type is the same as the parameter type.</p>
   * <p>There are however two exceptions:</p>
   * <ol>
   *   <li>If the parameter is not repeatable and {@code X} is of the form {@link java.util.Optional Optional&lt;Y&gt;},
   *   then the mapper must return {@code Y}</li>
   *   <li>If the parameter is {@link #repeatable},
   *   then the mapper must return the input type of the associated collector.
   *   In most cases, the type of a repeatable parameter will be of the form
   *   {@link java.util.List List&lt;E&gt;},
   *   and the default collector {@link java.util.stream.Collectors#toList() toList}
   *   will be used. Then the mapper must return {@code E}.</li>
   * </ol>
   */
  Class<? extends Supplier> mappedBy() default Supplier.class;

  /**
   * <p>The supplier must yield a {@link java.util.stream.Collector Collector&lt;M, ?, X&gt;}
   * where {@code X} is the parameter type, and {@code M} is the mapper type.
   * </p>
   * <p>This only makes sense for {@link #repeatable} arguments.</p>
   * <p>The default collector is the one that's returned by
   * {@link java.util.stream.Collectors#toList() Collectors.toList}.
   */
  Class<? extends Supplier> collectedBy() default Supplier.class;

  /**
   * <p>Declares this parameter repeatable.</p>
   */
  boolean repeatable() default false;

  /**
   * <p>Declares this parameter optional.</p>
   */
  boolean optional() default false;

  /**
   * <p>Declares a parameter that doesn't take an argument, like {@code -v}.</p>
   */
  boolean flag() default false;

  /**
   * The key used to find the command description in the resource bundle.
   * By default, the lowercased method name is used as the key.
   * If no bundle is defined, or this key is not in the bundle, then
   * the parameter method's javadoc is used as the description.
   */
  String bundleKey() default "";
}
