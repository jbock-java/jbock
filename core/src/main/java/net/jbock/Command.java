package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for an abstract class or interface
 * that defines a command line API.
 * Each abstract method in this class must have an empty
 * argument list, and carry exactly one of the
 * following annotations:
 *
 * <ul>
 *   <li>{@link Option}
 *   <li>{@link Parameter}
 *   <li>{@link Parameters}
 * </ul>
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface Command {

    /**
     * The program name to be used in the usage documentation.
     * If empty, a program name will be chosen based on the
     * class name of the annotated class.
     *
     * @return program name, or empty string
     */
    String name() default "";

    /**
     * Text to display before the synopsis block in the usage documentation.
     * If empty, the javadoc of the annotated class will be used as a fallback.
     *
     * @return description text
     */
    String[] description() default {};

    /**
     * The key that is used to find the command description
     * in the internationalization message map.
     *
     * @return key or empty string
     */
    String descriptionKey() default "";

    /**
     * If {@code true}, the generated parser will stop parsing after the
     * last positional parameter was read,
     * and return the remaining tokens as an array of strings.
     * The double-dash escape sequence
     * is then not recognized as a special token.
     *
     * <p>The following additional rules apply when
     * {@code superCommand = true}:
     *
     * <ul>
     *   <li>There must be at least one positional parameter.
     *   <li>Repeatable positional parameters are not allowed.
     * </ul>
     *
     * @return {@code true} to make this a SuperCommand
     */
    boolean superCommand() default false;

    /**
     * Disables clustering of unix short options.
     *
     * @return {@code false} to disable unix clustering
     */
    boolean unixClustering() default true;

    /**
     * If {@code false}, the generated parser will not contain
     * the {@code parseOrExit} method.
     *
     * @return {@code false} to skip generating {@code parseOrExit}
     */
    boolean generateParseOrExitMethod() default true;
}
