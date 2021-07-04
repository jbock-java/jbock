package net.jbock;

import net.jbock.util.StringConverter;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for a converter class.
 * If the converter is a (static) inner class of its command,
 * then this annotation may be omitted.
 *
 * <p>The converter class must extend {@link StringConverter StringConverter&lt;M&gt;} or
 * {@link java.util.function.Supplier Supplier&lt;StringConverter&lt;M&gt;&gt;},
 * where one of the following holds:
 *
 * <ul>
 *   <li>The return type of the referencing method is {@code M}.
 *   <li>The return type of the referencing method is a primitive type,
 *   and {@code M} is the corresponding boxed type.
 *   <li>The return type of the referencing method is {@code Optional<M>}.
 *   <li>The return type of the referencing method is one of the types {@code [OptionalInt, OptionalLong, OptionalDouble]},
 *   and {@code M} is the corresponding boxed primitive.
 *   <li>The return type of the referencing method is {@code List<M>}.
 * </ul>
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface Converter {
}
