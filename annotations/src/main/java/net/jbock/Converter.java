package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for a mapper class.
 * This annotation is mandatory only standalone classes.
 * Those are the classes that are not an inner class
 * of the class where the converter is referenced.
 * The mapper class must implement either {@code Function<String, M>} or
 * {@code Supplier<Function<String, M>>},
 * where one of the following holds:
 *
 * <ul>
 *   <li>The return type of the associated {@code abstract}
 *   parameter method is {@code M}.</li>
 *   <li>The return type of the associated {@code abstract}
 *   parameter method is a primitive type, and {@code M} is its boxed version.</li>
 *   <li>The return type of the associated {@code abstract}
 *   parameter method is {@code Optional<M>}.</li>
 *   <li>The return type of the associated {@code abstract}
 *   parameter method is one of the types {@code OptionalInt, OptionalLong, OptionalDouble}
 *   and {@code M} is the boxed version of the corresponding primitive type.</li>
 *   <li>The return type of the associated {@code abstract}
 *   parameter method is {@code List<M>}.</li>
 * </ul>
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface Converter {
}
