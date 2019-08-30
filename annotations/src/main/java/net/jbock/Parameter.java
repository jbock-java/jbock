package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h3>Marker for parameter methods</h3>
 *
 * <ul>
 * <li>The annotated method must be abstract and have an empty argument list.</li>
 * <li>The annotated method may not carry the {@link PositionalParameter} annotation.</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Parameter {

  /**
   * <p>Long names define 'gnu style' parameters that start with two dashes.
   * Long parameters can be passed in two different forms:</p>
   *
   * <ul>
   * <li>Attached: {@code --file=foo.txt}</li>
   * <li>Detached: {@code --file foo.txt}</li>
   * </ul>
   *
   * @return a valid long name
   */
  String longName() default "";

  /**
   * <p>Short names define arguments that consist of a single dash followed
   * by a single character.
   * The space character represents "no short name defined".
   * Short parameters can be passed in two different forms:</p>
   *
   * <ul>
   * <li>Attached: {@code -f=foo.txt}</li>
   * <li>Detached: {@code -f foo.txt}</li>
   * </ul>
   *
   * @return an alphanumeric character
   */
  char shortName() default ' ';

  /**
   * The argument name that's printed in the example usage,
   * when the user has passed the {@code --help} parameter.
   *
   * @return an example argument name for this parameter
   */
  String descriptionArgumentName() default "";

  /**
   * <h3>Optional custom mapper</h3>
   *
   * <p>
   * The mapper is a either a {@link java.util.function.Function Function&lt;String, X&gt;}
   * or a {@link java.util.function.Supplier Supplier} that returns such a function.
   * The return value {@code X} is called the <em>mapper type</em>.
   * The parameter method must return {@code X}, or {@code Optional<X>} if the
   * parameter is {@link #optional()}, or {@code List<X>} if the parameter is
   * {@link #repeatable()}, unless a custom collector is also used.
   * </p>
   *
   * <p>
   * For example, the following mapper parses and validates a positive number:
   * </p>
   *
   * <pre>{@code
   * class PositiveNumberMapper implements Function<String, Integer> {
   *
   *   public Integer apply(String s) {
   *     Integer r = Integer.valueOf(s);
   *     if (r <= 0) {
   *       throw new IllegalArgumentException("Positive number expected");
   *     }
   *     return r;
   *   }
   * }
   * }</pre>
   *
   * @return a mapper class
   */
  Class<?> mappedBy() default Object.class;

  /**
   * <h3>Optional custom collector</h3>
   *
   * <p>
   * This is either a {@link java.util.stream.Collector Collector&lt;M, ?, X&gt;}
   * where {@code X} is the parameter type and {@code M} is the <em>mapper type</em>,
   * or a {@link java.util.function.Supplier Supplier} that returns such a collector.
   * </p>
   *
   * <p>
   * For example, the following collector creates a {@code Set}:
   * </p>
   *
   * <pre>{@code
   * class ToSetCollector<E>; implements Supplier<Collector<E, ?, Set<E>>> {
   *
   *   public Collector<E, ?, Set<E>> get() {
   *     return Collectors.toSet();
   *   }
   * }
   * }</pre>
   *
   * @return an collector class
   */
  Class<?> collectedBy() default Object.class;

  /**
   * <p>If {@code true}, or if {@link #mappedBy} or {@link #collectedBy} are set,
   * declares that this parameter is repeatable.
   * If {@code false} or unspecified, and neither {@link #collectedBy} nor  {@link #mappedBy} are set,
   * the question whether or not this parameter is repeatable
   * will be answered by looking at the parameter's type.</p>
   *
   * <p>In the second case, the only parameter type that will lead to a repeatable parameter is {@link java.util.List}.</p>
   *
   * @return true to ensure that this parameter is repeatable
   */
  boolean repeatable() default false;

  /**
   * <p>If {@code true}, or if {@link #mappedBy} or {@link #collectedBy} are set,
   * declares whether this parameter is optional or required.
   * If {@code false} or unspecified, and neither {@link #collectedBy} nor  {@link #mappedBy} are set,
   * the question whether this parameter
   * is optional or required will be answered by looking at the parameter's type.</p>
   *
   * <p>In the second case, these are all the parameter types that will lead to an optional parameter:</p>
   *
   * <ul>
   * <li>{@link java.util.Optional}</li>
   * <li>{@link java.util.OptionalInt}</li>
   * <li>{@link java.util.OptionalLong}</li>
   * <li>{@link java.util.OptionalDouble}</li>
   * </ul>
   *
   * @return true to ensure that this parameter is optional
   */
  boolean optional() default false;

  /**
   * <p>If {@code true}, the parameter's type must be {@code boolean} or {@code Boolean}.
   * It will then be treated as a "flag" parameter that doesn't take an argument.
   * At runtime, the boolean value will indicate presence or absence of the parameter in the argument vector.
   * Repeating or "grouping" of flag parameters is not supported.</p>
   *
   * @return true if this parameter is a flag
   */
  boolean flag() default false;

  /**
   * <p>This key is used to find the parameter description in the resource bundle.</p>
   *
   * <p>The builder object that's returned by the
   * generated {@code parse} method can be used
   * to define the resource bundle at runtime.</p>
   *
   * <p>If no bundle key is defined, or no bundle is used,
   * then the parameter method's javadoc is used as the description.</p>
   *
   * @return an optional resource bundle key
   */
  String bundleKey() default "";
}

