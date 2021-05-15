[![annotations](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations/badge.svg?color=grey&style=plastic&subject=jbock-annotations)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations)
[![core](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg?style=plastic&subject=jbock)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

jbock is a command line parser inspired by [JCommander](https://jcommander.org/)
and [picocli](https://github.com/remkop/picocli).
It is an
[annotation processor](https://openjdk.java.net/groups/compiler/processing-code.html)
that generates custom parsing code at compile time.

Please see the [wiki](https://github.com/h908714124/jbock/wiki) for usage details.

### Basic example

A command line interface is defined as an `abstract` class 
which has a `@Command` or `@SuperCommand` annotation.
In this class, each `abstract` method corresponds either to a *named option* or a *positional parameter*.

````java
@Command
abstract class DeleteCommand {

  /**
   * The type is important!
   * Path, not Optional<Path>.
   * This parameter is required.
   */
  @Parameter(index = 0,
             description = "A positional parameter.")
  abstract Path path();

  /**
   * The type is important!
   * OptionalInt, not int or Integer.
   * This option is optional.
   */
  @Option(names = {"-v", "--verbosity"},
          description = "A named option.")
  abstract OptionalInt verbosity();
}
````

### Sample projects

* [jbock-maven-example](https://github.com/h908714124/jbock-maven-example)
* [jbock-gradle-example](https://github.com/h908714124/jbock-gradle-example)
