package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for parameter methods.
 * The parameter method must be abstract
 * and have an empty argument list.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Parameter {

  /**
   * A unique &quot;gnu style&quot; long name.
   * This definition doesn't contain the two dashes.
   *
   * @return a nonempty string
   */
  String value();

  /**
   * An optional mnemonic.
   *
   * @return a character
   */
  char mnemonic() default ' ';

  /**
   * Declare a custom mapper for this parameter.
   * This is either a
   * {@link java.util.function.Function Function}
   * accepting strings,
   * or a {@link java.util.function.Supplier Supplier} thereof.
   *
   * @return an optional mapper class
   */
  Class<?> mappedBy() default Object.class;

  /**
   * Declare a custom collector for a <em>repeatable</em> parameter.
   * This is either a {@link java.util.stream.Collector Collector}
   * or a {@link java.util.function.Supplier Supplier} thereof.
   *
   * @return an optional collector class
   */
  Class<?> collectedBy() default Object.class;

  /**
   * The key that is used to find the parameter
   * description in the i18 resource bundle for the online help.
   * If no such key is defined,
   * or if no bundle is supplied at runtime,
   * then an attempt is made to derive the parameter description
   * from the parameter method's javadoc.
   *
   * @return an optional bundle key
   */
  String bundleKey() default "";
}
