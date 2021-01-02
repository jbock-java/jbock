[![core](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg?style=plastic&subject=jbock)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)
[![annotations](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations/badge.svg?color=red&style=plastic&subject=jbock-annotations)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations)

jbock is a nifty command line parser that uses annotations similar to
[airline](https://github.com/airlift/airline) and
[picocli.](https://github.com/remkop/picocli)
It doesn't use reflection, but generates java source code at compile time instead.

A command line interface is expressed as an `abstract` class carrying the `@Command` annotation.
Here, each parameterless `abstract` method defines either a *named option* or a *positional parameter*.

````java
@Command
abstract class MyCommand {

  /**
   * A {@code @Param} is a positional parameter.
   * This particular param is in the first position,
   * since there are no other params in lower positions.
   */
  @Param(1)
  abstract Path path();

  /**
   * An {@code @Option} is a named option.
   */
  @Option("verbosity")
  abstract OptionalInt verbosity();
}
````

When jbock is properly configured as an
annotation processor, the presence of the command class
will trigger a round of code generation at compile time.
The generated class will, in this case, be called
`MyCommand_Parser`. It can be used as follows:

````java
String[] args = { "--verbosity", "2", "file.txt" }; // sample psvm input
MyCommand c = new MyCommand_Parser().parseOrExit(args);

// Working as expected!
assertEquals(OptionalInt.of(2), c.verbosity());
assertEquals(Paths.get("file.txt"), c.path());
````

In the MyCommand example, the `path` parameter is *required*,
while the option `verbosity` is *optional*.
The property of being either optional or required is called *skew*.
There are four different skews:
*required*, *optional*, *repeatable* and *flag*.
In this case the skew is
determined by the return type of the option's or parameter's `abstract` method,
according to the following rules.

### Skew rules

These rules apply for options and params that
define neither a custom mapper nor collector,
as in the `MyCommand` example:

#### Skew table A

Return type of the `abstract` method          | *Skew*
--------------------------------------------- | --------------------------------
`boolean` or `Boolean`                        | *flag* (only for `@Option`)
`Optional<A>`                                 | *optional*
`OptionalInt`,`OptionalLong`,`OptionalDouble` | *optional*
`List<A>`                                     | *repeatable*
`A` (exact match)                             | *required*

where `A` must be one of the
[auto types](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/hello/JbockAutoTypes.java),
otherwise compilation will fail.

If a custom mapper is defined, but no collector,
then the skew is determined by comparing the mapper's return type `M`
and the return type of the option's `abstract` method:

#### Skew table B

Mapper return type        | Return type of the `abstract` method          | *Skew*
------------------------- | --------------------------------------------- | ------------
`M`                       | `Optional<M>`                                 | *optional*
`Integer`,`Long`,`Double` | `OptionalInt`,`OptionalLong`,`OptionalDouble` | *optional*
`M`                       | `List<M>`                                     | *repeatable*
`M`                       | `M` (exact match)                             | *required*

If none of these rules apply, compilation will fail.

When a custom collector is defined, then its *input* type must be the mapper's return type,
or if no mapper is defined, it must be one of the auto types.
Its *output* type must be the return type of the `abstract` method.
The *skew* of a parameter with a custom collector is always *repeatable*.
This can be summarized in another table:

#### Meta skew rules

Mapper defined? | Collector defined? | *Skew*
--------------- | ------------------ | -----------
No              | No                 | See <a href="#skew-table-a">Skew Table A</a>
Yes             | No                 | See <a href="#skew-table-b">Skew Table B</a>
No              | Yes                | *repeatable*
Yes             | Yes                | *repeatable*

### Sample projects

* [jbock-maven-example](https://github.com/h908714124/jbock-maven-example)
* [jbock-gradle-example](https://github.com/h908714124/jbock-gradle-example)
