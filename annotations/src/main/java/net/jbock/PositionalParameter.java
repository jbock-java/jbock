package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

/**
 * <h3>Marker for positional parameter methods</h3>
 *
 * <ul>
 * <li>The annotated method must be abstract and have an empty argument list.</li>
 * <li>The annotated method may not carry the {@link Parameter} annotation.</li>
 * <li>If there is more than one positional parameter,
 * then the lexical ordering of these methods in the source file is relevant!</li>
 * </ul>
 *
 * <p>
 * <p>For example, the following shell commands contain positional parameters:</p>
 * <pre>{@code
 * ls ..
 * git log
 * echo 'First positional parameter' 'Second positional parameter'
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface PositionalParameter {

  /**
   * see {@link Parameter#argHandle()}
   *
   * @return an optional arg handle
   */
  String argHandle() default "";

  /**
   * Optional custom mapper.
   * See {@link Parameter#mappedBy()}
   *
   * @return an optional mapper class
   */
  Class<? extends Supplier> mappedBy() default Supplier.class;

  /**
   * Optional custom collector.
   * See {@link Parameter#collectedBy()}
   *
   * @return an optional collector class
   */
  Class<? extends Supplier> collectedBy() default Supplier.class;

  /**
   * <p>Declares this parameter repeatable.</p>
   *
   * <ul>
   * <li>There can only be one positional repeatable parameter.</li>
   * <li>The repeatable positional parameter must be the last positional parameter,
   * in the lexical ordering of the Java source file.</li>
   * </ul>
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
   * see {@link Parameter#bundleKey()}
   *
   * @return an optional resource bundle key
   */
  String bundleKey() default "";
}
