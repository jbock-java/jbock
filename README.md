[![core](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg?style=plastic&subject=jbock)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)
[![annotations](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations/badge.svg?color=red&style=plastic&subject=jbock-annotations)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations)

jbock is a simple yet flexible command line parser that uses the same annotation names as
[airline](https://github.com/airlift/airline) and
[picocli.](https://github.com/remkop/picocli)
It doesn't make use of Java's reflection framework, but generates java source code instead that can be easily read and debugged.

In your model "command" class, options and params are defined as `abstract` methods:

````java
@Command
abstract class MyArguments {

  /**
   * A "param" is a positional parameter.
   * The number 1 determines its position relative to the other params. 
   * In this case it is irrelevant, as there is only one param.
   */
  @Param(1)
  abstract Path path();

  /**
   * An option is a named parameter or flag.
   * The name is passed with one or two leading dashes:
   * ["--verbosity=1", "--verbosity 1", "-v 1", "-v1"] are all valid.
   * Note: This javadoc will show up when "--help" is passed.
   * Alternatively the help text can be taken from a resource bundle.
   */
  @Option(value = "verbosity", mnemonic = 'v')
  abstract OptionalInt verbosity();
}
````

jbock must be configured as an
[annotation processor.](https://stackoverflow.com/questions/2146104/what-is-annotation-processing-in-java)
The presence of a command class
will trigger a round of code generation at compile time.
The resulting class
[MyArguments_Parser](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/hello/MyArguments_Parser.java)
can be used as follows:

````java
String[] args = { "-v", "2", "file.txt" }; // command line parameters
MyArguments my = new MyArguments_Parser().parseOrExit(args);

// works as expected
assertEquals(OptionalInt.of(2), my.verbosity());
assertEquals(Paths.get("file.txt"), my.path());
````

In the example above, `path` is a *required* param,
while `verbosity` is an *optional* option.
This so-called *parameter skew* is determined by the parameter type,
as described in the following section.

### Skew rules

These are the rules for options and params that
define neither a custom mapper nor collector:

Parameter type                      | Skew
----------------------------------- | --------------------------------
`boolean` or `Boolean`              | *flag* (only applies to options)
`X` (exact match)                   | *required*
`Optional<X>`                       | *optional*
`List<X>`                           | *repeatable*
<code>Optional{Int&#124;Long&#124;Double}</code> | *optional*

where `X` is one of the
[auto types.](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/hello/JbockAutoTypes.java)

If a custom mapper is defined, but no collector, then these rules apply:

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
