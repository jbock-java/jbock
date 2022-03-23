[![jbock-compiler](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/jbock-compiler/badge.svg?color=grey&subject=jbock-compiler)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/jbock-compiler)
[![jbock](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/jbock/badge.svg?subject=jbock)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/jbock)

jbock is a command line parser, which uses the same annotation names as [JCommander](https://jcommander.org/)
and [picocli](https://github.com/remkop/picocli).
However it does not use reflection.
It is an
[annotation processor](https://openjdk.java.net/groups/compiler/processing-code.html)
that generates a custom parser at compile time.

### Quick start

Create an abstract class, or alternatively a Java interface,
and add the `@Command` annotation.
In this class, each abstract method must have no arguments,
and be annotated with either `@Option`, `@Parameter` or `@VarargsParameter`.
The *multiplicity* of options and parameters is determined by their return type.

````java
@Command
abstract class DeleteCommand {


  /* `OptionalInt` (or alternatively `Optional<Integer>`),
   * but not `int` or `Integer`:
   * This named option is optional (multiplicity = 0..1).
   * Note: List<Integer> for multiplicity = 0..n.
   */
  @Option(names = {"-v", "--verbosity"},
          description = "A named option.")
  abstract OptionalInt verbosity();

  /* `Path`, not `Optional<Path>`:
   * This positional parameter is required (multiplicity = 1).
   */
  @Parameter(index = 0,
             description = "A positional parameter.")
  abstract Path path();

  @VarargsParameter(description = "This must be a list.")
  abstract List<Path> morePaths();
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

In addition to `parseOrExit`, a basic `parse` method is generated
that you can build upon to fine-tune the help and error messages for your users.
If you need more types, custom String-converters can be defined.
Please see the [wiki](https://github.com/h908714124/jbock/wiki) for details.

### Sample projects

* [jbock-maven-example](https://github.com/jbock-java/jbock-maven-example)
* [jbock-gradle-example](https://github.com/jbock-java/jbock-gradle-example)

