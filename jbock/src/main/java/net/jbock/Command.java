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
 * argument list, and carry one of the following annotations:
 *
 * <ul>
 *   <li>{@link Option}
 *   <li>{@link Parameter}
 *   <li>{@link VarargsParameter}
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
     * Optional text to display before the synopsis block in the usage documentation.
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
     * If {@code true}, the generated parser will not contain
     * the {@code parseOrExit} method.
     *
     * @return {@code true} to skip generating the
     *         {@code parseOrExit} method
     */
    boolean skipGeneratingParseOrExitMethod() default false;

    /**
     * If {@code true}, the generated {@code parseOrExit} method
     * will accept {@code List<String>} instead of {@code String[]}.
     */
    boolean parseOrExitMethodAcceptsList() default false;

    /**
     * Set to {@code true} to enable at-file expansion.
     */
    boolean enableAtFileExpansion() default false;

    /**
     * If {@code true}, the generated parser class will be
     * {@code public}. Otherwise, it will be package-private.
     *
     * @return {@code true} to generate a public parser class
     */
    boolean publicParser() default false;
}
