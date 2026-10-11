package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for a named option.
 * The annotated method must be nullary and abstract.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Option {

    /**
     * A list of unique option names.
     * A name can be either a gnu name, prefixed with two dashes,
     * or a unix name. A unix name consists of single dash, followed by
     * a single-character option name.
     * A named option must have at least one option name.
     *
     * @return option names
     */
    String[] names() default {};

    /**
     * The key that is used to find the option
     * description in the internationalization bundle.
     *
     * @return key or empty string
     */
    String descriptionKey() default "";

    /**
     * Description text to be used when generating the usage documentation.
     * Can be overridden via {@code descriptionKey}.
     *
     * @return description text
     */
    String[] description() default {};

    /**
     * A label to represent a unary option's argument in the usage documentation.
     * If empty, a label will be chosen based on the {@link #names}.
     *
     * <p>Note: Mode flags ({@code boolean} options) do not take an argument.
     *          Their label will be ignored.
     *
     * @return description label for the option argument
     */
    String paramLabel() default "";
}
