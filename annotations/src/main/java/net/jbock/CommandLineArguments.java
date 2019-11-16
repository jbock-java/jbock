package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks an abstract model class
 * that contains parameter methods.
 * Each of its abstract methods must be annotated with either the
 * {@link Parameter} or the {@link PositionalParameter} annotation.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CommandLineArguments {

  /**
   * The program name that is used in the online help,
   * when the user passes the {@code --help} parameter.
   *
   * @return an optional program name
   */
  String programName() default "";

  /**
   * If {@code true}, the special token {@code --help}
   * will be understood by the generated parser,
   * if it is passed as the very first argument.
   *
   * @return false to disable the {@code --help} parameter
   */
  boolean allowHelpOption() default true;
}
