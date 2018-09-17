package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

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

  Class<? extends Function> mappedBy() default Function.class;
}
