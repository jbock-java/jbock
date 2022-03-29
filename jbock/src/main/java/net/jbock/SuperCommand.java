package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for an abstract class or interface
 * that defines a command line API.
 *
 * <p>Each abstract method of the command class must have an empty
 * argument list, and either an {@link Option} or a {@link Parameter} annotation.
 * There must be at least one {@code Parameter}. The {@link VarargsParameter} annotation
 * is not allowed.
 *
 * <p>The generated parser will stop parsing after the
 * last {@code Parameter} was read. On the command line,
 * the user has to make sure that all options are passed before the last {@code Parameter}.
 *
 * <p>The generated methods {@code parse} and {@code parseOrExit}
 * will return the remaining tokens, after the last {@code Parameter},
 * as an array of strings, ready to be passed on to
 * another command line parser.
 *
 * <p>The generated parser will not recognize the double-dash escape sequence.
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface SuperCommand {

    /**
     * @see Command#name()
     */
    String name() default "";

    /**
     * @see Command#description()
     */
    String[] description() default {};

    /**
     * @see Command#descriptionKey()
     */
    String descriptionKey() default "";

    /**
     * @see Command#skipGeneratingParseOrExitMethod()
     */
    boolean skipGeneratingParseOrExitMethod() default false;

    /**
     * @see Command#publicParser()
     */
    boolean publicParser() default false;
}
