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
   * <p>Long names define 'gnu style' parameters that start with two dashes.</p>
   *
   * <p>Example:</p>
   *
   * <pre>{@code
   * date --iso-8601
   * }</pre>
   *
   * @return a valid long name
   */
  String longName() default "";

  /**
   * <p>Short names define arguments that consist of a single dash followed
   * by a single character.</p>
   *
   * <p>Example:</p>
   *
   * <pre>{@code
   * curl -X 'Accept: application/json'
   * }</pre>
   *
   * @return an alphanumeric character
   */
  char shortName() default ' ';

  /**
   * The argument name that's printed in the example usage.
   *
   * @return an optional name that's used in the parameter description
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
   * <p>Declares this parameter as repeatable.</p>
   *
   * @return true if this parameter is repeatable
   */
  boolean repeatable() default false;

  /**
   * <p>Declares this parameter as optional.</p>
   *
   * <p>
   * <em>Note:</em>
   * Parameters are required by default. However,
   * {@link #repeatable()} and {@link #flag()}
   * parameters are always optional.
   *</p>
   *
   * @return true if this parameter is optional
   */
  boolean optional() default false;

  /**
   * <p>Declares a parameter that doesn't take an argument.
   * For example, the following shell command contains the flag {@code -l}:</p>
   *
   * <pre>{@code
   * ls -l
   * }</pre>
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

