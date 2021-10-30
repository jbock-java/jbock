[![jbock-compiler](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/jbock-compiler/badge.svg?color=grey&subject=jbock-compiler)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/jbock-compiler)
[![jbock](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/jbock/badge.svg?subject=jbock)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/jbock)

jbock is a command line parser, which uses the same annotation names as [JCommander](https://jcommander.org/)
and [picocli](https://github.com/remkop/picocli).
However it does not use reflection.
It is an
[annotation processor](https://openjdk.java.net/groups/compiler/processing-code.html)
that generates a custom parser at compile time.
jbock requires Java 11.

### Quick start

Create an abstract class, or alternatively a Java interface,
and add the `@Command` annotation.
In this class or interface, each abstract method corresponds either to a *named option* or a *positional parameter*.

````java
@Command
abstract class DeleteCommand {

  /* Path, not Optional<Path>:
   * This positional parameter is required (multiplicity = 1).
   */
  @Parameter(index = 0,
             description = "A positional parameter.")
  abstract Path path();

  /* OptionalInt, not int or Integer:
   * This named option is optional (multiplicity = 0..1).
   */
  @Option(names = {"-v", "--verbosity"},
          description = "A named option.")
  abstract OptionalInt verbosity();
}
````

The following two classes will be generated:
[DeleteCommandParser](https://github.com/jbock-java/jbock-docgen/blob/master/src/main/java/com/example/hello/DeleteCommandParser.java),
[DeleteCommand_Impl](https://github.com/jbock-java/jbock-docgen/blob/master/src/main/java/com/example/hello/DeleteCommand_Impl.java).

The `*Parser` class can be used directly in a `main` method:

````java
public static void main(String[] args) {
    DeleteCommand command = new DeleteCommandParser().parseOrExit(args);
    // ...
}

````

In addition to `parseOrExit`, there is also a basic `parse` method with no side effects.
Please see the [wiki](https://github.com/h908714124/jbock/wiki) for details.

### Sample projects

* [jbock-maven-example](https://github.com/jbock-java/jbock-maven-example)
* [jbock-gradle-example](https://github.com/jbock-java/jbock-gradle-example)

