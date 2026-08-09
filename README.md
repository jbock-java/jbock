[![jbock-compiler](https://img.shields.io/maven-central/v/io.github.jbock-java/jbock-compiler?label=jbock-compiler)](https://central.sonatype.com/artifact/io.github.jbock-java/jbock-compiler)
[![jbock](https://img.shields.io/maven-central/v/io.github.jbock-java/jbock?label=jbock)](https://central.sonatype.com/artifact/io.github.jbock-java/jbock)


jbock is a command line parser, which uses well-known annotation names similar to [JCommander](https://jcommander.org/)
and [picocli](https://github.com/remkop/picocli).
It is an
[annotation processor](https://openjdk.java.net/groups/compiler/processing-code.html)
which does not use runtime reflection, but generates a custom parser at compile time instead.

### Quick rundown

Choose any name for the java interface which will describe your command line API.
Add the `@Command` anntation to your command interface, to make the annotation processor aware of it.

Let's call a non-default interface method an *abstract method*.
Each abstract method in your command interface represents a command line option or argument.
It must have "getter signature": return something other that `void`, and have an empty parameter list.
It must also be annotated with either
`@Option`, `@Parameter` or `@VarargsParameter` (with one exception, see below).

The return types `boolean`, `List<?>` and `Optional<?>` (including `OptionalInt` and such) have special semantics.
They are used to declare flags, repeable and optional options and parameters, respectively.

> [!TIP]
> If a method annotated with `@VarargsParameter` exists, it must return a list.
> There cannot be more than one such method.
> Any unannotated, list-returning abstract method is also used as a catch-all for extra positional parameters.
> In other words, the `@VarargsParameter` annotation can be omitted.

Here's an example:

````java
@Command
interface DeleteCommand {

  @Option(names = {"-v", "--verbosity"},
          description = {"The return type for this option is \"optionalish\".",
                         "Using int or Integer instead would make it a required option."})
  OptionalInt verbosity();

  @Parameter(
          index = 0,
          description = {"A required positional parameter.",
                         "Path is a \"known type\",",
                         "so the parser knows how to create a Path from a string."})
  Path path();

  @Parameter(
          index = 1,
          description = {"The return type for this positional parameter is \"optionalish\",",
                         "which makes this an optional positional parameter"})
  Optional<Path> anotherPath();

  @VarargsParameter(
          description = {"The varargs parameter. There can be at most one of these.",
                         "It is a catch-all for additional positional parameters.",
                         "Must return List<Something>.",
                         "Note: For an actual rm-style interface, it might make more sense",
                         "to have only the VarargsParameter, and no other Parameters."})
  List<Path> morePaths();
  
  @Option(names = "--dry-run",
          description = {"The return type for this option is boolean,",
                         "which makes it a mode flag."})
  boolean dryRun();
  
  @Option(names = "-h",
          description = "This returns List<Something>, so it's a \"repeatable option\".")
  List<String> headers(); 
  
  @Option(names = "--charset",
          description = {"Charset is not a \"known type\",",
                         "but we can use a default method to convert. See below."})
  Optional<String> charsetString();
  
  // A default method can be used to convert from string.
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

Some types are converted automatically. See [StandardConverters.java](https://github.com/jbock-java/jbock/blob/master/jbock/src/main/java/net/jbock/contrib/StandardConverters.java).

### Subcommands

The `@SuperCommand` annotation can be used to define a git-like subcommand structure. See [javadoc](https://github.com/jbock-java/jbock/blob/master/jbock/src/main/java/net/jbock/SuperCommand.java).

### Sample projects

* [jbock-maven-example](https://github.com/jbock-java/jbock-maven-example)
* [jbock-gradle-example](https://github.com/jbock-java/jbock-gradle-example)

### Alternatives

* [Tim's list](https://github.com/timtiemens/javacommandlineparser)
