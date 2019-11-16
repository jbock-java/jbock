package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for non-positional parameters.
 * The annotated method must be abstract and have an empty argument list.
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
   * <p>Declare a custom mapper for this parameter.
   * This is either a {@link java.util.function.Function Function&lt;String, X&gt;}
   * or a {@link java.util.function.Supplier Supplier} of such a function.
   * </p>
   *
   * <p>For example, the following mapper parses a positive number:
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
   * <p>Declare a custom collector for a <em>repeatable</em> parameter.</p>
   *
   * <p>This is either a {@link java.util.stream.Collector Collector&lt;M, ?, X&gt;}
   * where {@code X} is the parameter type and {@code M} is the <em>mapper type</em>,
   * or a {@link java.util.function.Supplier Supplier} that returns such a collector.
   * </p>
   *
   * <p>Parameters with a custom collector are always treated as repeatable.</p>
   *
   * <p>For example, the following collector creates a {@code Set}:
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
   * @return a collector class
   */
  Class<?> collectedBy() default Object.class;


  /**
   * The key that is used to find the parameter
   * description in the resource bundle.
   * If no bundle key is defined, or no bundle is used at runtime,
   * then the parameter method's javadoc is used as the description.
   *
   * @return an optional resource bundle key
   */
  String bundleKey() default "";
}

