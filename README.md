[![core](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg?style=plastic&subject=jbock)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)
[![annotations](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations/badge.svg?color=red&style=plastic&subject=jbock-annotations)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations)

jbock is a command line parser.
While other tools like [airline](https://github.com/airlift/airline) and
[picocli.](https://github.com/remkop/picocli) scan for annotations at runtime, jbock is an
[annotation processor](https://openjdk.java.net/groups/compiler/processing-code.html)
that generates all the parsing code at compile time already.

### Overview

A command line interface is defined by an `abstract` class which has a `@Command` annotation.
In this class, each `abstract` method corresponds either to a *named option* or a *positional parameter*.

````java
@Command
abstract class MyCommand {

  /**
   * A positional parameter.
   */
  @Param(0)
  abstract Path path();

  /**
   * A named option.
   */
  @Option("verbosity")
  abstract OptionalInt verbosity();
}
````

When jbock is properly configured as an
annotation processor, the presence of the `@Command` annotation
will trigger a round of code generation at compile time.
The generated class will, in this case, be called
`MyCommand_Parser`. It can be used as follows:

````java
String[] args = { "--verbosity=2", "file.txt" }; // sample psvm input
MyCommand c = new MyCommand_Parser().parseOrExit(args);

// Works as expected!
assertEquals(OptionalInt.of(2), c.verbosity());
assertEquals(Paths.get("file.txt"), c.path());
````

In the MyCommand example, the `path` parameter is *required*,
while the option `verbosity` is *optional*.
The property of being either optional or required is called *skew*.
There are four different skews:
*required*, *optional*, *repeatable* and *flag*.
In this case, the skew is
determined by the return type of the option's or parameter's `abstract` method,
according to the following rules.

### Skew rules

These rules apply for options and params that
do not define a custom mapper,
as in the `MyCommand` example:

#### Skew table A

Return type of the `abstract` method      | *Skew*
----------------------------------------- | --------------------------------
`{boolean,Boolean}`                       | *flag* (only for `@Option`)
`Optional<A>`                             | *optional*
`Optional{Int,Long,Double}`               | *optional*
`List<A>`                                 | *repeatable*
`A` (exact match)                         | *required*

where `A` must be one of the
[auto types](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/hello/JbockAutoTypes.java),
otherwise compilation will fail.

Both `@Option` and `@Param` have an optional attribute
`mappedBy` which takes a single value of type `Class<?>`.
Any such mapper class must implement `Function<String, E>` or `Supplier<Function<String, E>>` for some `E`.

If a custom mapper is defined,
then the skew is determined by comparing
the method's return type to the type of its mapper:

#### Skew table B

Mapper type                                     | Return type of the `abstract` method | *Skew*
----------------------------------------------- | ------------------------------------ | ------------
Function&lt;String, `M`&gt;                     | `Optional<M>`                        | *optional*
Function&lt;String, `{Integer,Long,Double}`&gt; | `Optional{Int,Long,Double}`          | *optional*
Function&lt;String, `M`&gt;                     | `List<M>`                            | *repeatable*
Function&lt;String, `M`&gt;                     | `M`                                  | *required*
Function&lt;String, `{Integer,Float,...}`&gt;   | `{int,float,...}`                    | *required*

When none of these rules apply, compilation will fail.

Both rule tables can be summarized in a third table:

#### Skew rules overview

Mapper defined? | *Skew*
--------------- | -----------
No              | See <a href="#user-content-skew-table-a">Skew Table A</a>
Yes             | See <a href="#user-content-skew-table-b">Skew Table B</a>

### Sample projects

* [jbock-maven-example](https://github.com/h908714124/jbock-maven-example)
* [jbock-gradle-example](https://github.com/h908714124/jbock-gradle-example)
