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
 *   <li>The annotated type must be an abstract class.</li>
 *   <li>There must be at least one abstract method.</li>
 *   <li>Each abstract method must have an empty argument list.</li>
 *   <li>The class may not extend or implement anything, other than {@link java.lang.Object}.</li>
 * </ul>
 *
 * <p>Each abstract method in the annotated class must return one of these types:</p>
 *
 * <ul>
 *   <li>{@code boolean}</li>
 *   <li>{@code String}</li>
 *   <li>{@code Optional<String>}</li>
 *   <li>{@code List<String>}</li>
 *   <li>{@code int}</li>
 *   <li>{@code OptionalInt}</li>
 * </ul>
 *
 * <p>
 *   If the method carries the {@link Positional} annotation,
 *   it may also not return {@code boolean}.
 * </p>
 *
 * @see <a href="https://github.com/h908714124/jbock">jbock on github</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CommandLineArguments {

  /**
   * <p>
   * Should unknown tokens be interpreted as positional arguments,
   * even if they start with a hyphen character?
   * </p><p>
   * If {@code false}, an unknown free token that starts with a hyphen
   * will cause the parsing to fail with an {@link IllegalArgumentException}.
   * </p><p>
   * If {@code true}, an unknown free token that starts with a hyphen
   * will be read as a positional argument.
   * For example, it is then possible to pass a negative number
   * as a positional argument.
   * </p>
   */
  boolean ignoreDashes() default false;

  /**
   * General usage information that is printed when the user passes the {@code --help} parameter.
   * The overview is printed at the beginning of the {@code DESCRIPTION} section of the usage information.
   */
  String[] overview() default {};

  /**
   * <p>
   * The name of the final executable program.
   * If the java program is usually invoked from a wrapper script,
   * then this should be the file name of that script.
   * </p><p>
   * The program name is printed when the user passes the
   * {@code --help} parameter, in the {@code NAME} section of the usage information.
   * By default, the short name of the annotated java class is used as the program name.
   * If that class is an inner class, the short name of its enclosing class is the default program name.
   * </p>
   */
  String programName() default "";

  /**
   * A short, one-sentence summary of the program. It is printed when the user passes the
   * {@code --help} parameter, in the {@code NAME} section of the usage information.
   */
  String missionStatement() default "";

  /**
   * <p>
   * If {@code true}, the {@code --help} parameter doesn't have a special function.
   * When this option is enabled, it becomes possible to define a parameter with the
   * long name {@code "help"}. In this case, the full usage information
   * is always printed when there is a user error in the command line syntax.
   * </p>
   */
  boolean helpDisabled() default false;
}
