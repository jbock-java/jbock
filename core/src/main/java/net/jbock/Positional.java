package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *   This annotation is used by the jbock annotation processor.
 * </p>
 *
 * <p>The annotated method must be abstract and have
 *   an empty argument list. It may not return {@code boolean}.</p>
 *
 * <h2>Ordering matters!</h2>
 *
 * <p>When this annotation is used, the ordering of methods
 * in the source type makes a difference.</p>
 *
 * <p>Some orderings are invalid:
 * For example, a positional method that returns {@code List<String>}
 * must be declared <em>below</em> one that returns {@code String}.</p>
 *
 * <p>If there are two positional methods that return {@code List<String>},
 * then the second list will contain all tokens after &quot;double dash&quot;
 * in the input.</p>
 *
 * <p>
 *   The enclosing class must be annotated with {@link CommandLineArguments}.
 * </p>
 *
 * @see <a href="https://github.com/h908714124/jbock">jbock on github</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Positional {
}
