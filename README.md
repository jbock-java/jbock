[![core](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg?style=plastic&subject=jbock)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)
[![annotations](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations/badge.svg?color=red&style=plastic&subject=jbock-annotations)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations)

jbock is a simple yet flexible command line parser that uses the same annotation names as
[airline](https://github.com/airlift/airline) and
[picocli.](https://github.com/remkop/picocli)
The following example shows how to declare your command line arguments:

````java
@Command
abstract class MyArguments {

  /**
   * A "param" is a positional parameter.
   * The number 1 is arbitrary as long as there is only one param.
   */
  @Param(1)
  abstract Path path();

  /**
   * This javadoc will show up when "--help" is passed.
   * Alternatively you can define the help text in a resource bundle.
   */
  @Option(value = "verbosity", mnemonic = 'v')
  abstract OptionalInt verbosity();
}
````

jbock must be configured as an
[annotation processor.](https://stackoverflow.com/questions/2146104/what-is-annotation-processing-in-java)
The presence of the annotated class above
will trigger a round of code generation at compile time.
The resulting class
[MyArguments_Parser](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/hello/MyArguments_Parser.java)
can be used as follows:

````java
String[] args = { "-v", "2", "file.txt" }; // command line parameters
MyArguments my = new MyArguments_Parser().parseOrExit(args);

// make sure this works as expected...
assertEquals(OptionalInt.of(2), my.verbosity());
assertEquals(Paths.get("file.txt"), my.path());
````

Please note that `path` is a *required* param,
while `verbosity` is an *optional* option.
This so-called *parameter skew* is determined from the parameter type,
using the following "matching" rules:

### Parameter type matching

If neither mapper nor collector are explicitly defined, the following matching rules apply:

Parameter type                      | Skew
----------------------------------- | --------------------------------
`boolean` or `Boolean`              | *flag* (only applies to options)
`X` (exact match)                   | *required*
`Optional<X>` or `OptionalInt` etc. | *optional*
`List<X>`                           | *repeatable*

where `X` is one of the
[auto types.](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/hello/JbockAutoTypes.java)

If an explicit mapper is defined, but no collector, then the following rules apply:

Mapper return type      | Parameter type              | Skew
----------------------- | --------------------------- | ------------
`R`                     | `R` (exact match)           | *required*
`R`                     | `Optional<R>`               | *optional*
`R`                     | `List<R>`                   | *repeatable*
`Integer`               | `OptionalInt`               | *optional*
`Long`                  | `OptionalLong`              | *optional*
`Double`                | `OptionalDouble`            | *optional*

If a custom collector is defined, then the skew is always *repeatable*.

* [Detailed documentation](https://github.com/h908714124/jbock/blob/master/SPAGHETTI.md)
* [jbock-maven-example](https://github.com/h908714124/jbock-maven-example)
* [jbock-gradle-example](https://github.com/h908714124/jbock-gradle-example)
