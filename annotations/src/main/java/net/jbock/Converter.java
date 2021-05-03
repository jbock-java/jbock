package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * <p>Marker annotation for a converter class.
 * If the converter class is defined as a static inner class inside
 * the class where it is referenced,
 * then this annotation may be omitted.</p>
 *
 * <p>The converter must implement either {@code Function<String, M>} or
 * {@code Supplier<Function<String, M>>},
 * where one of the following holds:</p>
 *
 * <ul>
 *   <li>The return type of the referencing method is {@code M}.</li>
 *   <li>The return type of the referencing method is a primitive type,
 *   and {@code M} is the corresponding boxed type.</li>
 *   <li>The return type of the referencing method is {@code Optional<M>}.</li>
 *   <li>The return type of the referencing method is one of the types {@code [OptionalInt, OptionalLong, OptionalDouble]},
 *   and {@code M} is the corresponding boxed primitive.</li>
 *   <li>The return type of the referencing method is {@code List<M>}.</li>
 * </ul>
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface Converter {
}
