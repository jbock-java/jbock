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
 * <li>The annotated method must not also carry the {@link Parameter} annotation.</li>
 * <li>The method's enclosing class must be annotated with {@link CommandLineArguments}.</li>
 * <li>When this annotation is used on more than one parameter,
 * then the lexical ordering of these methods in the source file is relevant.</li>
 * <li>A {@link #repeatable} positional parameter may only appear once.</li>
 * <li>A {@link #repeatable} positional parameter must be the last positional parameter,
 * in lexical order.</li>
 * <li>If the parameter type is of the form {@link java.util.Optional Optional&lt;X&gt;}, then this parameter must
 * appear, in lexical order, after all positional parameters that are not
 * {@link #repeatable} and not of this form.
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
   * <p>The supplier must yield a {@link java.util.function.Function Function&lt;String, X&gt;}
   * where {@code X} is called the <em>mapper type</em>.
   * In many cases, the mapper type is the same as the parameter type.</p>
   * <p>There are however two exceptions:</p>
   * <ol>
   * <li>If the parameter is not repeatable and {@code X} is of the form {@link java.util.Optional Optional&lt;Y&gt;},
   * then the mapper must return {@code Y}</li>
   * <li>If the parameter is {@link #repeatable},
   * then the mapper must return the input type of the associated collector.
   * In most cases, the type of a repeatable parameter will be of the form
   * {@link java.util.List List&lt;E&gt;},
   * and the default collector {@link java.util.stream.Collectors#toList() toList}
   * will be used. Then the mapper must return {@code E}.</li>
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
   * The key used to find the command description in the resource bundle.
   * By default, the lowercased method name is used as the key.
   * If no bundle is defined, or this key is not in the bundle, then
   * the parameter method's javadoc is used as the description.
   */
  String commandDescriptionKey() default "";
}
