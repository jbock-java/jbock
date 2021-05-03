package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * <p>Marker annotation for a repeatable positional parameter.
 * This parameter will capture the remaining tokens,
 * after all non-repeatable positional parameters have been captured.</p>
 *
 * <ul>
 *   <li>The annotated method must be {@code abstract} and have an empty argument list.</li>
 *   <li>It must return {@link java.util.List List&lt;E&gt;}, where {@code E} is a converted type.</li>
 *   <li>The method's enclosing class must carry the {@link Command} or {@link SuperCommand} annotation.</li>
 *   <li>There cannot be more than one such method per class.</li>
 * </ul>
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Parameters {

  /**
   * Declare a custom converter that will be applied to each
   * individual token that's captured by this parameter.
   * This is either a
   * {@link java.util.function.Function Function}
   * accepting strings,
   * or a {@link java.util.function.Supplier Supplier} of such a function.
   * It must either be a {@code static} inner class of the class carrying the {@link Command} annotation,
   * or, if it is declared in a separate source file, it must carry the {@link Converter} annotation.
   *
   * @return converter class or {@code Void.class}
   */
  Class<?> converter() default Void.class;

  /**
   * The key that is used to look up the parameter
   * description in the internationalization resource bundle for the online help.
   * If no such key is defined,
   * or no bundle is supplied at runtime,
   * or the bundle supplied at runtime does not contain the bundle key,
   * then the {@code abstract} method's javadoc is used as the param's description.
   *
   * @return bundle key or empty string
   */
  String bundleKey() default "";
}

