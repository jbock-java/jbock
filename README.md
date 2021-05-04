[![core](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg?style=plastic&subject=jbock)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)
[![annotations](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations/badge.svg?color=red&style=plastic&subject=jbock-annotations)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations)

jbock is a command line parser inspired by [jcommander](https://jcommander.org/)
and [picocli](https://github.com/remkop/picocli).
It is an
[annotation processor](https://openjdk.java.net/groups/compiler/processing-code.html)
that generates custom parsing code at compile time.

### Overview

A command line interface is defined as an `abstract` class 
which has a `@Command` or `@SuperCommand` annotation.
In this class, each `abstract` method corresponds either to a *named option* or a *positional parameter*.

````java
@Command
abstract class DeleteCommand {

  /**
   * A positional parameter.
   */
  @Parameter(index = 0)
  abstract Path path();

  /**
   * A named option.
   */
  @Option(names = {"-v", "--verbosity"})
  abstract OptionalInt verbosity();
}
````

The generated class will, in this case, be called
`DeleteCommand_Parser`. It can be used as follows:

````java
String[] args = { "-v2", "file.txt" };
DeleteCommand c = new DeleteCommand_Parser().parseOrExit(args);

// Works as expected!
assertEquals(OptionalInt.of(2), c.verbosity());
assertEquals(Paths.get("file.txt"), c.path());
````

In the above example, the `path` parameter is *required*,
while `verbosity` is *optional*.
The `@Parameter` and `@Option` annotations do not have a flag
to mark something as required or optional.
Instead, the code generator looks at the annotated method's 
return type to figure this out.

#### Skew table A: Auto conversion

These rules apply for both `path` and `verbosity` in the `DeleteCommand` example:

Return type of annotated method           | *Skew*
----------------------------------------- | --------------------------------
`boolean`                                 | *flag* (only `@Option`)
`Optional<A>`                             | *optional*
`Optional{Int,Long,Double}`               | *optional*
`List<A>`                                 | *repeatable* (`@Option` or `@Parameters`)
`A` (exact match)                         | *required*

where `A` must be one of the
[auto types](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/hello/JbockAutoTypes.java),
otherwise compilation will fail.

In the `DeleteCommand` example, the type of `path` is `java.nio.file.Path`, which is one of the
auto types, so the last rule applies, making this a required parameter.
The type of `verbosity` is `OptionalInt`, so the third rule applies,
which makes this an optional option.

Both the `@Option` and `@Parameter` annotations also have an optional attribute
`converter`, which takes a single value of type `Class<?>`.
A converter class must implement `Function<String, E>` or `Supplier<Function<String, E>>` for some `E`.

#### Skew table B: Converter attribute is set

If a custom converter is defined,
then the skew is determined by looking at both the return type and the converter type.

Type of the converter                           | Return type of annotated method      | *Skew*
----------------------------------------------- | ------------------------------------ | ------------
Function&lt;String, `M`&gt;                     | `Optional<M>`                        | *optional*
Function&lt;String, `{Integer,Long,Double}`&gt; | `Optional{Int,Long,Double}`          | *optional*
Function&lt;String, `M`&gt;                     | `List<M>`                            | *repeatable*
Function&lt;String, `M`&gt;                     | `M`                                  | *required*
Function&lt;String, `{Integer,Float,...}`&gt;   | `{int,float,...}`                    | *required*

When none of these rules apply, compilation will fail.

### Sample projects

* [jbock-maven-example](https://github.com/h908714124/jbock-maven-example)
* [jbock-gradle-example](https://github.com/h908714124/jbock-gradle-example)
