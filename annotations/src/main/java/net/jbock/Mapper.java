package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for a mapper class.
 * It must implement either {@code Function<String, M>} or
 * {@code Supplier<Function<String, M>>}
 * where one of the following holds:
 * <ul>
 *   <li>The return type of the associated {@code abstract}
 *   parameter method is {@code M}.</li>
 *   <li>The return type of the associated {@code abstract}
 *   parameter method is {@code Optional<M>}.</li>
 *   <li>The return type of the associated {@code abstract}
 *   parameter method is {@code List<M>}.</li>
 * </ul>
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface Mapper {

}
