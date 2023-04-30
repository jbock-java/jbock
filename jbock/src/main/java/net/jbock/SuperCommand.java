package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Marker annotation for an abstract class or interface
 * that defines a command line API, similar to {@link Command},
 * with some differences as explained below.
 *
 * <p>Each abstract method of the command class must have an empty
 * argument list, and either the {@link Option} or the {@link Parameter} annotation.
 * There must be at least one {@code Parameter}. The {@link VarargsParameter} annotation
 * is not allowed on a super-command.
 *
 * <p>The generated parser will stop parsing after the
 * last {@code Parameter} has been read.
 * The generated methods {@code parse} and {@code parseOrExit}
 * will return the command instance and also the remaining tokens,
 * after the last {@code Parameter}, as an array of strings.
 *
 * <p>The generated parser will not recognize the double-dash escape sequence.
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface SuperCommand {

    /**
     * @return program name
     * @see Command#name()
     */
    String name() default "";

    /**
     * @return introductory text for the usage documentation
     * @see Command#description()
     */
    String[] description() default {};

    /**
     * @return internationalization key
     * @see Command#descriptionKey()
     */
    String descriptionKey() default "";

    /**
     * @return {@code true} to skip generating the {@code parseOrExit} method
     * @see Command#skipGeneratingParseOrExitMethod()
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
     * @return {@code true} if public parser should be generated
     * @see Command#publicParser()
     */
    boolean publicParser() default false;
}
