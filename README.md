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
The *multiplicity* of options and parameters is determined by their return type. `List` and `Optional` are "special".

````java
@Command
abstract class DeleteCommand {

  @Option(names = {"-v", "--verbosity"},
          description = "A named option. The return type reflects optionality.")
  abstract OptionalInt verbosity();

  @Parameter(
          index = 0,
          description = "A required positional parameter. Return type is non-optional.")
  abstract Path path();

  @Parameter(
          index = 1,
          description = "An optional positional parameter.")
  abstract Optional<Path> anotherPath();

  @VarargsParameter(
          description = {"A varargs parameter. Only one per command allowed.",
                         "The return type must be List.")
  abstract List<Path> morePaths();
  
  @Option(names = "--dry-run",
          description = "A nullary option, a.k.a. mode flag. Return type is boolean.")
  abstract boolean dryRun();
  
  @Option(names = "-h",
          description = "A repeatable option. Return type is List.")
  abstract List<String> headers(); 
  
  @Option(names = "--charset",
          description = "Named option with a custom converter",
          converter = CharsetConverter.class)
  abstract Optional<Charset> charset();
  
  // sample converter class
  static class CharsetConverter extends StringConverter<Charset> {
    @Override
    protected Charset convert(String token) { return StandardCharsets.UTF_8; }
  }
}
````

The generated `DeleteCommandParser` converts a string array to an instance of `DeleteCommand`:

````java
public static void main(String[] args) {
    DeleteCommand command = new DeleteCommandParser().parseOrExit(args);
    // ...
}

````

In addition to `parseOrExit`, the generated parser has a basic `parse` method 
that you can build upon to fine-tune the help and error messages for your users.

### Sample projects

* [jbock-maven-example](https://github.com/jbock-java/jbock-maven-example)
* [jbock-gradle-example](https://github.com/jbock-java/jbock-gradle-example)

