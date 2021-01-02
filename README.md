[![core](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg?style=plastic&subject=jbock)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)
[![annotations](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations/badge.svg?color=red&style=plastic&subject=jbock-annotations)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations)

jbock is a nifty command line parser that uses annotations similar to
[airline](https://github.com/airlift/airline) and
[picocli.](https://github.com/remkop/picocli)
It doesn't use reflection, but generates java source code at compile time instead.

A command line interface is expressed through a so-called *command* class.
Here, each *named option* and *positional parameter* corresponds to one annotated `abstract` method.

````java
@Command
abstract class MyArguments {

  /**
   * A positional parameter in position 1.
   * This is the first position, as there are no params in lower positions.
   */
  @Param(1)
  abstract Path path();

  /**
   * A named option.
   */
  @Option("verbosity")
  abstract OptionalInt verbosity();
}
````

When jbock is properly configured as an
[annotation processor](https://stackoverflow.com/questions/2146104/what-is-annotation-processing-in-java), the presence of the command class
will trigger a round of code generation at compile time.
The generated class will, in this case, be called
`MyArguments_Parser`. It can be used as follows:

````java
String[] args = { "--verbosity", "2", "file.txt" }; // command line parameters
MyArguments my = new MyArguments_Parser().parseOrExit(args);

// works as expected
assertEquals(OptionalInt.of(2), my.verbosity());
assertEquals(Paths.get("file.txt"), my.path());
````

In the example above, `path` is a *required* parameter,
while `verbosity` is *optional*.
The property of being either optional or required is called *skew*.
There are four different skews:
*required*, *optional*, *repeatable* and *flag*.
The skew is mostly
determined by the return type of the option's or parameter's `abstract` method,
according to the following rules:

### Skew rules

These are the rules for options and params that
define neither a custom mapper nor collector.

Return type of the `abstract` method  | Skew
------------------------------------- | --------------------------------
`boolean` or `Boolean`                | *flag* (only applies to options)
`Optional<A>`                         | *optional*
<code>Optional{Int&#124;Long&#124;Double}</code> | *optional*
`List<A>`                             | *repeatable*
any other                             | *required*

where `A` must be one of the
[auto types.](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/hello/JbockAutoTypes.java)

If a custom mapper is defined, but no collector,
then the skew is determined by comparing the mapper's return type
and the return type of the option's `abstract` method:

Mapper return type      | Return type of the `abstract` method | Skew
----------------------- | ------------------------------------ | ------------
`M`                     | `Optional<M>`                        | *optional*
`Integer`               | `OptionalInt`                        | *optional*
`Long`                  | `OptionalLong`                       | *optional*
`Double`                | `OptionalDouble`                     | *optional*
`M`                     | `List<M>`                            | *repeatable*
`M`                     | `M` (exact match, or via boxing)     | *required*

When a custom collector is defined, the skew is always *repeatable*.

* [Detailed documentation](https://github.com/h908714124/jbock/blob/master/SPAGHETTI.md)
* [jbock-maven-example](https://github.com/h908714124/jbock-maven-example)
* [jbock-gradle-example](https://github.com/h908714124/jbock-gradle-example)
