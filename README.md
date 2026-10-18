[![jbock-compiler](https://img.shields.io/maven-central/v/io.github.jbock-java/jbock-compiler?label=jbock-compiler)](https://central.sonatype.com/artifact/io.github.jbock-java/jbock-compiler)
[![jbock](https://img.shields.io/maven-central/v/io.github.jbock-java/jbock?label=jbock)](https://central.sonatype.com/artifact/io.github.jbock-java/jbock)


jbock is a command line parser, which uses well-known annotation names, similar to those used by
[JCommander](https://jcommander.org/)
and [picocli](https://github.com/remkop/picocli).

jbock is an
[annotation processor](https://openjdk.java.net/groups/compiler/processing-code.html).
It does not rely on runtime reflection, but generates java source code instead.

### Quick start

Choose any name for the java interface which will describe your command line API.
Add the `@Command` anntation to your command interface, to make the annotation processor aware of it.

Each abstract (i.e. non-`default`) method in your command interface represents a command line option or argument.
It must have "getter signature": return something other that `void`, and have an empty parameter list.
It must also be annotated with either
`@Option` or `@Parameter`.

The return types `boolean`, `List<?>` and `Optional<?>` (including `OptionalInt` and such) have special semantics.
They are used to declare flags, repeable and optional options and parameters, respectively.

> [!TIP]
> Only the exact type `java.util.List` has special semantics.
> An abstract method returning `ArrayList<String>`, for example, does *not* represent a
> repeatable option or catch-all parameter.

An example:

````java
@Command
interface DeleteCommand {

  /**
   * The return type of this method is &quot;optionalish&quot;,
   * so this is an optional option.
   * Using int or Integer instead would make it a required option.
   */
  @Option(names = {"-v", "--verbosity"})
  OptionalInt verbosity();

  /**
   * A required positional parameter.
   * It captures the first positional parameter.
   * The index attribute can be omitted, since it is
   * clear from the method's position in the source file.
   */
  @Parameter(index = 0)
  Path path();

  /**
   * The return type of this method is &quot;optionalish&quot;,
   * so this is an optional positional parameter.
   * It captures the second positional parameter, if available.
   * The index attribute can be omitted.
   */
  @Parameter(index = 1)
  Optional<Path> anotherPath();

  /**
   * The return type of this parameter is java.util.List,
   * so this is the catch-all parameter.
   * It captures all but the first two positional parameters.
   * The index attribute {@code index = 2} is omitted here.
   */
  @Parameter
  List<Path> morePaths();
  
  /**
   * The return type is boolean, so this is a mode flag.
   */
  @Option(names = "--dry-run")
  boolean dryRun();
  
  /**
   * Charset is not one of the &quot;known types&quot;,
   * so we use a default method to convert manually.
   * See below.
   */
  @Option(names = "--charset")
  Optional<String> charsetString();
  
  /**
   * This default method converts from String to Charset.
   */
  default Charset charset() {
      return Charset.forName(charsetString());
  }
}
````

The generated class is called `DeleteCommandParser`. We can use it in our main method:

````java
public static void main(String[] args) {
  DeleteCommand command = DeleteCommandParser.parseOrExit(args);
  // alternatively:
  // Either<ParsingFailed, DeleteCommand> either = DeleteCommandParser.parse(List.of(args));
  // more cool stuff...
}

````

### Known types

Some types are converted automatically. All of these are listed in [StandardConverters.java](https://github.com/jbock-java/jbock/blob/master/jbock/src/main/java/net/jbock/contrib/StandardConverters.java).

### Sample projects

* [jbock-maven-example](https://github.com/jbock-java/jbock-maven-example)
* [jbock-gradle-example](https://github.com/jbock-java/jbock-gradle-example)

### Alternatives

* [Tim's list](https://github.com/timtiemens/javacommandlineparser)
