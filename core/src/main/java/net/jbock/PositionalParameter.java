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
 * <li>The annotated method must be abstract and have an empty argument list.</li>
 * <li>The enclosing class must be annotated with {@link CommandLineArguments}.</li>
 * <li>The annotated method must not also carry the {@link Parameter} annotation.</li>
 * <li>When this annotation is used on more than one parameter,
 * then the lexical ordering of these methods in the source file is relevant.</li>
 * <li>A List&lt;X&gt; - typed positional parameter may only appear once.</li>
 * <li>A List&lt;X&gt; - typed positional parameter must be the last positional parameter,
 * in lexical source file order.</li>
 * <li>An Optional&lt;X&gt; - typed positional parameter must appear after all non-optional
 * positional parameters.</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface PositionalParameter {

  /**
   * <p>The argument name that's printed in the description.</p>
   *
   * <p>An empty string is special syntax for automatic parameter name,
   * which will be based on the method name.</p>
   */
  String argHandle() default "";

  /**
   * <p>The supplier must yield a mapper Function that maps string to the parameter method's return type.</p>
   * <p>There are two exceptions to this rule:</p>
   * <ol>
   * <li>if the parameter is optional, then the mapper must return the element type of the Optional</li>
   * <li>if the parameter is repeatable, then the mapper must return the element type of the List, or more generally,
   * the input type of the associated collector</li>
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
