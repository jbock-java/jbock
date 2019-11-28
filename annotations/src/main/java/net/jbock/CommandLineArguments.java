package net.jbock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks an abstract model class
 * that contains parameter methods.
 * Each of its abstract methods must be annotated with either
 * {@link Parameter} or {@link PositionalParameter}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CommandLineArguments {

  /**
   * The program name that is printed
   * when the {@code --help} token is encountered.
   *
   * @return an optional program name
   */
  String programName() default "";

  /**
   * If this flag is left at its default value {@code false},
   * then the generated parser will print the online help,
   * if {@code --help} is passed as the first argument.
   *
   * @return {@code true} to disable the {@code --help} mechanism
   */
  boolean helpDisabled() default false;
}
