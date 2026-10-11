package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for a repeatable or &quot;catch-all&quot; positional parameter.
 * This will capture the remaining positional parameters
 * after all other {@link Parameter}s have been captured.
 *
 * <ul>
 *   <li>The annotated method must be nullary and abstract.
 *   <li>The annotated method <em>must</em> return {@link java.util.List List&lt;E&gt;},
 *       where {@code E} is a &quot;known type&quot;.
 *   <li>There cannot be more than one catch-all parameter.
 *   <li>If no attributes are specified, this annotation can be omitted.
 * </ul>
 *
 * @see Parameter
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface VarargsParameter {

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
     * A label for the varargs parameter, to address them in the usage documentation.
     * If empty, a label will be chosen based on the method name.
     *
     * @return description label for the varargs parameter
     */
    String paramLabel() default "";
}
