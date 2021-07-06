package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for a <em>repeatable</em> positional parameter.
 * This parameter will capture the remaining tokens,
 * after all other positional parameters have been read.
 *
 * <ul>
 *   <li>The annotated method must be {@code abstract} and have an empty argument list.
 *   <li>It must return {@link java.util.List List&lt;E&gt;}, where {@code E} is a converted type.
 *   <li>There cannot be more than one repeatable positional parameter per command.
 *   <li>Cannot be used when the {@link Command#superCommand()} attribute is set.
 * </ul>
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Parameters {

    /**
     * Class of a custom converter for this repeatable parameter.
     * This is either a class that extends
     * {@link net.jbock.util.StringConverter StringConverter}
     * or a {@link java.util.function.Supplier Supplier} of a {@code StringConverter}.
     *
     * <p>Note: The same converter instance will be used to convert
     * each individual token.
     *
     * @return converter class or {@code Void.class}
     */
    Class<?> converter() default Void.class;

    /**
     * The key that is used to find the description for these
     * parameters in the internationalization bundle.
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
     * A label for this repeatable parameter, to address it in the usage documentation.
     * If empty, a label will be chosen based on the method name.
     *
     * @return a label
     */
    String paramLabel() default "";
}

