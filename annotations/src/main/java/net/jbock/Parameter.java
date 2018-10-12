package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

/**
 * <h2>Marker for parameter methods</h2>
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
   * *
   *
   * @return a valid long name
   */
  String longName() default "";

  /**
   * <p>Short names define arguments that start with a single dash followed
   * by a single character.</p>
   *
   * @return an alphanumeric character
   */
  char shortName() default ' ';

  /**
   * <p>The argument name that's printed in the example usage.</p>
   *
   * @return an optional arg handle
   */
  String argHandle() default "";

  /**
   * <p>Optional custom mapper</p>
   *
   * <p>The mapper is a {@link java.util.function.Supplier Supplier} that returns a
   * {@link java.util.function.Function Function&lt;String, X&gt;}.
   * The return value {@code X} is called the <em>mapper type</em>.
   * The parameter method must return {@code X}, or {@code Optional<X>} if the
   * parameter is {@link #optional()}, or {@code List<X>} if the parameter is
   * {@link #repeatable()}. There are even more options if a custom collector is used.
   * <p>
   * For example, the following mapper parses and validates a positive number:
   *
   * <pre>{@code
   * class PositiveNumberMapper implements Supplier&lt;Function&lt;String, Integer&gt;&gt; {
   *
   *   public Function&lt;String, Integer&gt; get() {
   *     return s -> {
   *       Integer r = Integer.valueOf(s);
   *       if (r <= 0) {
   *         throw new IllegalArgumentException("Positive number expected");
   *       }
   *       return r;
   *     }
   *   }
   * }
   * }</pre>
   *
   * @return an optional mapper class
   */
  Class<? extends Supplier> mappedBy() default Supplier.class;

  /**
   * <p>Optional custom collector</p>
   *
   * <p>The supplier must return a {@link java.util.stream.Collector Collector&lt;M, ?, X&gt;}
   * where {@code X} is the parameter type, and {@code M} is the <em>mapper type</em>.
   * </p>
   * <p>
   * For example, the following collector creates a {@code Set}:
   *
   * <pre>{@code
   * class ToSetCollector&lt;E&gt; implements Supplier&lt;Collector&lt;E, ?, Set&lt;E&gt;&gt;&gt; {
   *
   *   public Collector&lt;E, ?, Set&lt;E&gt;&gt; get() {
   *     return Collectors.toSet();
   *   }
   * }
   * }</pre>
   *
   * @return an optional collector class
   */
  Class<? extends Supplier> collectedBy() default Supplier.class;

  /**
   * <p>Declares this parameter repeatable.</p>
   *
   * @return true if this parameter is repeatable
   */
  boolean repeatable() default false;

  /**
   * <p>Declares this parameter optional.</p>
   *
   * @return true if this parameter is optional
   */
  boolean optional() default false;

  /**
   * <p>Declares a parameter that doesn't take an argument.
   * For example, the following shell command contains a flag:
   *
   * <pre>{@code
   * ls -l
   * }</pre>
   *
   * @return true if this parameter is a flag
   */
  boolean flag() default false;

  /**
   * This key is used to find the parameter description in the resource bundle.
   * By default, the bundle key will be based on the method name.
   * If no bundle is used, the parameter method's javadoc is used as the description.
   *
   * @return an optional resource bundle key
   */
  String bundleKey() default "";
}
