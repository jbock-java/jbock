package net.jbock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * <p>Marker annotation for an abstract class or interface
 * that defines a command line API.
 * Each abstract method in this class must have an empty
 * argument list, and carry exactly one of the
 * following annotations:</p>
 *
 * <ul>
 *   <li>{@link Option}</li>
 *   <li>{@link Parameter}</li>
 *   <li>{@link Parameters}</li>
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
   * <p>If {@code true},
   * the generated parser will print the usage documentation
   * when {@code --help} is the only input token,
   * or when there is at least one required item
   * and the input array is empty.</p>
   *
   * @return {@code false} to disable the help option
   */
  boolean helpEnabled() default true;

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
   * <p>Enables or disables the so-called {@code @file}
   * (read: &quot;at-file&quot;) expansion, a mechanism
   * that allows putting some or all command line options in
   * a configuration file.</p>
   *
   * @see net.jbock.util.AtFileReader
   *
   * @return {@code false} to disable the {@code @file} expansion
   */
  boolean atFileExpansion() default true;

  /**
   * <p>If {@code true}, the generated parser will stop parsing after the
   * last positional parameter was read,
   * and return the remaining tokens as an array of strings.
   * The double-dash escape sequence
   * is then not recognized as a special token.</p>
   *
   * <p>The following additional rules apply when
   * {@code superCommand = true}:</p>
   *
   * <ul>
   *   <li>There must be at least one positional parameter.</li>
   *   <li>Repeatable positional parameters are not allowed.</li>
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
}
