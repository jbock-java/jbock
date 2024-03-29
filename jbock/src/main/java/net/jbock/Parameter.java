package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for a positional parameter that is <em>not</em> repeatable.
 * Use {@link VarargsParameter} to declare a repeatable positional parameter.
 *
 * <p>The annotated method must be abstract
 * and have an empty argument list.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Parameter {

    /**
     * The parameter's index among the command's positional parameters.
     * The first parameter's index must be {@code 0}.
     *
     * @return zero-based index
     */
    int index();

    /**
     * Class of a custom converter for this positional parameter.
     * This is either a class that extends
     * {@link net.jbock.util.StringConverter StringConverter}
     * or a {@link java.util.function.Supplier Supplier} of a {@code StringConverter}.
     *
     * @return converter class or {@code Void.class} to represent &quot;none&quot;
     */
    Class<?> converter() default Void.class;

    /**
     * The key that is used to find the parameter
     * description in the internationalization bundle.
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
