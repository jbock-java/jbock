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
 * <ul>
 *   <li>The annotated method must be abstract.</li>
 *   <li>The annotated method must have an empty argument list.</li>
 *   <li>The enclosing class must be annotated with {@link CommandLineArguments}.</li>
 *   <li>The annotated method must not also carry the {@link Parameter} annotation.</li>
 *   <li>When this annotation is used on more than one parameter, the ordering of methods in the source type will make a difference.</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface PositionalParameter {
}
