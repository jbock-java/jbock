package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for a named option.
 * The annotated method must be abstract
 * and have an empty argument list.
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
     * @return list of option names
     */
    String[] names() default {};

    /**
     * <p>Class of a custom converter for this named option.
     * This is either a class that extends
     * {@link net.jbock.util.StringConverter StringConverter}
     * or a {@link java.util.function.Supplier Supplier} of a string converter.</p>
     * <p>Note for repeatable options: The same converter instance
     * will be used to convert all relevant tokens in the input array.</p>
     *
     * @return converter class or {@code Void.class}
     */
    Class<?> converter() default Void.class;

    /**
     * The key that is used to find the option
     * description in an internationalization bundle.
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
     * <p>A label to represent the option's argument in the usage documentation.
     * If empty, a label will be chosen based on the {@link #names}.</p>
     * <p>Note: Mode flags do not take an argument. Their label will be ignored.</p>
     *
     * @return a label
     */
    String paramLabel() default "";
}
