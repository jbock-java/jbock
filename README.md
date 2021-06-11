[![jbock-compiler](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/jbock-compiler/badge.svg?color=grey&style=plastic&subject=jbock-compiler)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/jbock-compiler)
[![jbock](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/jbock/badge.svg?style=plastic&subject=jbock)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/jbock)

jbock is a command line parser, similar to [JCommander](https://jcommander.org/)
and [picocli](https://github.com/remkop/picocli).
It is an
[annotation processor](https://openjdk.java.net/groups/compiler/processing-code.html)
that generates custom parsing code at compile time.

### Basic example

A command line interface is defined as an `abstract` class 
which has a `@Command` annotation.
In this class, each `abstract` method defines a *named option* or a *positional parameter*.

````java
@Command(name = "rm", description = "Coffee time!")
abstract class DeleteCommand {

  /* Path, not Optional<Path>:
   * This positional parameter is required.
   */
  @Parameter(index = 0,
             description = "A positional parameter.")
  abstract Path path();

  /* OptionalInt, not int or Integer:
   * This named option is optional.
   */
  @Option(names = {"-v", "--verbosity"},
          description = "A named option.")
  abstract OptionalInt verbosity();
}
````

See here for the code this generates:
[DeleteCommandParser.java](https://github.com/jbock-java/jbock-docgen/blob/master/src/main/java/com/example/hello/DeleteCommandParser.java)

The generated parser is usually used in the `main` method:

````java
public static void main(String[] args) {
    DeleteCommand command = new DeleteCommandParser().parseOrExit(args);
    // ...
}

````

In addition to `parseOrExit`, there is also a highly configurable `parse` method with no side effects.
Please see the [wiki](https://github.com/h908714124/jbock/wiki) for details.

### Sample projects

* [jbock-maven-example](https://github.com/jbock-java/jbock-maven-example)
* [jbock-gradle-example](https://github.com/jbock-java/jbock-gradle-example)

