package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * This annotation is used by the jbock annotation processor.
 * </p>
 *
 * <ul>
 * <li>The annotated type must be an abstract class.</li>
 * <li>There must be at least one abstract method.</li>
 * <li>Each abstract method must have an empty argument list.</li>
 * <li>The class may not extend or implement anything, other than {@link java.lang.Object}.</li>
 * </ul>
 *
 * <p>Each abstract method in the annotated class must return one of these types:</p>
 *
 * <ul>
 * <li>{@code boolean}</li>
 * <li>{@code String}</li>
 * <li>{@code Optional<String>}</li>
 * <li>{@code List<String>}</li>
 * <li>{@code int}</li>
 * <li>{@code OptionalInt}</li>
 * </ul>
 *
 * <p>
 * If the method carries the {@link Positional} annotation,
 * it must not return {@code boolean}.
 * </p>
 *
 * @see <a href="https://github.com/h908714124/jbock">jbock on github</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CommandLineArguments {

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
   * If {@code true}, a special parameter {@code --help} will be understood by the parser.
   * In this case, it is an error to assign this long name anywhere.
   * </p>
   */
  boolean addHelp() default true;

  /**
   * True if a double dash "--" stops option parsing.
   * The remaining tokens will be treated as positional.
   */
  boolean allowEscape() default true;

  /**
   * True if unknown tokens that start with a dash should be permissible.
   * These tokens will then be treated as positional.
   * Otherwise these tokens are treated as bad input.
   *
   * Note that setting {@link #allowEscape()} makes the double dash "--" a known token.
   */
  boolean strict() default true;

}
