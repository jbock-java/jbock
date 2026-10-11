package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for a positional parameter.
 *
 * <p>The annotated method must be nullary and abstract.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Parameter {

    /**
     * The parameter's position, relative to the other positional parameters.
     * The indexes must start with {@code 0}, and must not contain any gaps.
     *
     * <p>If the index is {@code -1}, the actual index is auto-assigned
     * based on the method's position relative to the other parameter methods
     * in the source file.
     *
     * @return a zero-based index or {@code -1}
     */
    int index() default -1;

    /**
     * The key that is used to find the parameter
     * description in the i18n bundle.
     *
     * @return description key or empty string
     */
    String descriptionKey() default "";

    /**
     * Parameter description, used when generating the usage documentation.
     * Can be overridden via {@code descriptionKey}.
     *
     * @return description text
     */
    String[] description() default {};

    /**
     * A label for this parameter, to address it in the usage documentation.
     * If empty, a label will be chosen based on the method name.
     *
     * @return description label for this parameter
     */
    String paramLabel() default "";
}
