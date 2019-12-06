### Contents

* <a href="#features-overview">Features overview</a>
* <a href="#parameter-types">Parameter types</a>
* <a href="#positional-parameters">Positional parameters</a>
* <a href="#flags">Flags</a>
* <a href="#binding-parameters">Binding parameters</a>
* <a href="#escape-sequence">Escape sequence</a>
* <a href="#repeatable-parameters">Repeatable parameters</a>
* <a href="#parameter-shapes">Parameter shapes</a>
* <a href="#showing-help">Showing help</a>
* <a href="#standard-coercions">Standard coercions</a>
* <a href="#custom-mappers-and-parameter-validation">Custom mappers and parameter validation</a>
* <a href="#custom-collectors">Custom collectors</a>
* <a href="#parameter-descriptions-and-internationalization">Parameter descriptions and internationalization</a>
* <a href="#parsing-failure">Parsing failure</a>
* <a href="#runtime-modifiers">Runtime modifiers</a>
* <a href="#limitations">Limitations</a>
* <a href="#gradle-config">Gradle config</a>
* <a href="#maven-config">Maven config</a>
* <a href="#running-tests">Running tests</a>

### Features overview

Some of the features, especially the handling of optional parameters, may be unexpected
for users of similar parsers:

1. In the Java model, <a href="https://github.com/h908714124/jbock/blob/master/README.md#parameter-type-matching">optional parameters</a>
   correspond to methods that return [Optional](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html).
   Coincidentally, there is no way to make a parameter method return `null`.
1. <a href="#binding-parameters">*Binding parameters*</a> are always [unary:](https://en.wikipedia.org/wiki/Unary_operation)
    1. The parameter name must be followed by a single argument.
       If we think of the `argv` array as a map,
       then this means that there must be exactly one value per key.
    1. These key-value pairs can also be written as <a href="#parameter-shapes">*single tokens.*</a>
    1. Parameters can be <a href="#repeatable-parameters">*repeatable.*</a>
       Repeatable parameters mostly correspond to methods that return [List,](https://en.wikipedia.org/wiki/Java_collections_framework)
       but there can be exceptions from this rule if a custom mapper or collector is defined
       (see [README.md](https://github.com/h908714124/jbock/blob/master/README.md#parameter-type-matching))

Next, we look at some of the features in more detail.

### Parameter types

Command line applications have access to a special array of strings,
which is often called `args` or `argv`.
This represents the command line parameters that are passed to the application at runtime.

Some of the tokens in this array take the form of key-value pairs.
These are called "Options" or "non-positional parameters".
Others may be distinguished by their position.

Now we take a closer look at the basic parameter types:

1. <a href="#positional-parameters">*Positional parameters*</a>
1. Options
    1. nullary: <a href="#flags">*Flags*</a>
    1. unary: <a href="#binding-parameters">*Binding parameters*</a>

### Positional parameters

A *positional* parameter is just a value, without a
preceding parameter name. The value is not allowed
to start with a "minus" character. Here is an example:

````java
@Command
abstract class MyArguments {

  @Param(1)
  abstract Path source();
  
  @Param(2)
  abstract Path target();
}
````

The `MyArguments_Parser` that is generated
from this example requires an `argv` of length *exactly* `2`,
because none of the parameters are `Optional`.

The `source` parameter has the *lowest* position,
so it will bind the *first* token.
Here is a valid usage example:

````java
String[] args = { "a.txt", "b.txt" };
MyArguments my = MyArguments_Parser.create().parseOrExit(args);
assertEquals(Paths.get("a.txt"), my.source());
assertEquals(Paths.get("b.txt"), my.target());
````

### Flags

These are the simplest non-positional parameters. 

To declare a flag, simply
declare an option method that returns
`boolean` or `Boolean`.

````java
@Option(value = "quiet", mnemonic = 'q')
abstract boolean quiet();
````

At runtime, the method will return `true`
if either `--quiet` or `-q`
are <a href="#binding-parameters">*free*</a> in `argv`.

````java
MyArguments args = MyArguments_Parser.create().parseOrExit(new String[]{ "-q" });
assertTrue(args.quiet());
args = MyArguments_Parser.create().parseOrExit(new String[]{ "--quiet" });
assertTrue(args.quiet());
````

### Binding parameters

An *Option* that is not a <a href="#flags">*flag*</a> is called a
*binding parameter*. For example, the following
method declares a simple binding parameter:

````java
// example of a binding parameter
@Option("file")
abstract String file();
````

The bound token can be an arbitrary string. Any token in `argv` that is not bound by some
binding parameter, and precedes the
<a href="#escape-sequence">*escape sequence*</a>, is called *free*.

### Escape sequence

The escape sequence consists of the <a href="#binding-parameters">*free*</a> token `"--"`,
i.e. two consecutive "dash" characters. 
Any remaining tokens in `argv` after that will be treated as <a href="#positional-parameters">*positional*</a>.
In other words, the escape sequence *ends option parsing*.
The generated parser recognizes the escape sequence
if there is at least one positional parameter defined.

### Repeatable parameters

Repeatable parameters are either <a href="#binding-parameters">*binding*</a>
or <a href="#positional-parameters">*positional*</a> parameters
that can appear any number of times in `argv`. For example:

````java
@Option("headers")
abstract List<String> headers();
````

This list will contain the headers in the same order
in which they appear in `argv`.

To declare a repeatable parameter, either define a custom collector, or
use a parameter method that returns `List<E>`, where `E` is the
mapper return type.

### Parameter shapes

Given a <a href="#binding-parameters">*binding parameter*</a> like this

````java
@Option(value = "file", mnemonic = 'f')
abstract Path file();
````

then we have the long form and the mnemonic, which are equivalent

````java
String[] argv;
argv = { "--file", "data.txt" }; // two dashes -> long form
argv = { "-f", "data.txt" }; // one dash -> mnemonic
````

Binding parameters can also be written in *attached* form as follows

````java
argv = { "--file=data.txt" }; // attached long form
argv = { "-fdata.txt" }; // attached mnemonic
````

Note: If both `value` and `mnemonic` are defined, there
are *four* different ways to write a binding parameter.

On the other hand, if both names are defined,
there are only *two* ways to write a <a href="#flags">*flag:*</a>

````java
argv = { "--quiet" }; // two dashes -> flag long form
argv = { "-q" }; // one dash -> flag mnemonic
````

### Showing help

The token `--help` has a special meaning, if it is the first token in `argv`. 

````java
String[] argv = { "--help" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);
````

This time, `parseOrExit` will shut down the JVM with an exit code of `0`, and print
usage information to standard out. Any remaining tokens in `argv`
will then be ignored.

To disable the special meaning of the `--help` token, use
`@CommandLineArguments(helpDisabled = true)`. 

### Standard coercions

All non-private enums, as well as
[some standard Java types](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/helloworld/JbockAllTypes.java)
can be used as parameter types, without having
to write a custom mapper first. Optional and List of these
types are also allowed.

### Custom mappers and parameter validation

Mappers (a.k.a. converters) must implement [Function](https://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html)`<`[String](https://docs.oracle.com/javase/8/docs/api/java/lang/String.html)`, ?>`,
or a [Supplier](https://docs.oracle.com/javase/8/docs/api/java/util/function/Supplier.html) of such a function,
where `?` depends on the parameter it's used on.
The mapper's input is the parameter value taken from the argument vector `String[] argv`.
If the parameter does not appear in `argv`, then the mapper is not invoked.
The mapper may reject its input by throwing any [RuntimeException](https://docs.oracle.com/javase/8/docs/api/java/lang/RuntimeException.html).

````java
class PositiveNumberMapper implements Function<String, Integer> {

  @Override
  public Integer apply(String s) { // the input string will not be null
    Integer i = Integer.valueOf(s); // exceptions are ok, no try-catch needed
    if (i <= 0) {
      throw new IllegalArgumentException("Try to keep it positive.");
    }
    return i; // returning null is not recommended
  }
}
````

*Note: The mapper class must have a no-argument constructor.*

### Custom collectors

By using a custom collector, it is possible to create a
`Set`, or `Map` or other collections. The following example
builds a `Map`:

````java
@Parameter(value = "headers",
           mappedBy = MapTokenizer.class,
           collectedBy = MapCollector.class)
abstract Map<String, String> headers();
````

The mapper splits tokens of the form `a:b` into map entries

````java
class MapTokenizer implements Function<String, Map.Entry<String, String>> {

  @Override
  public Map.Entry<String, String> apply(String s) {
    String[] tokens = s.split(":", 2);
    if (tokens.length < 2) {
      throw new IllegalArgumentException("Invalid pair: " + s);
    }
    return new AbstractMap.SimpleImmutableEntry<>(tokens[0], tokens[1]);
  }
}
````

The collector class must be a [Collector](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collector.html)`<A, ?, B>`,
or a `Supplier` of such a collector,
where `A` is the output of the mapper, and `B` is the
parameter type.

````java
class MapCollector<K, V> implements Supplier<Collector<Map.Entry<K, V>, ?, Map<K, V>>> {

  @Override
  public Collector<Map.Entry<K, V>, ?, Map<K, V>> get() {
    return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
  }
}
````

This can be tested as follows

````java
String[] argv = { "-Xhorse:12", "-Xsheep:4" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

assertEquals(2, args.headers());
assertEquals("12", args.headers().get("horse"));
assertEquals("4", args.headers().get("sheep"));
````

### Parameter descriptions and internationalization

By default, the method's Javadoc is used as the parameter description. 
Alternatively a resource bundle can be used, which overrides the
javadoc if the bundle contains a translation for the JVM's locale:

````java
MyArguments args = MyArguments_Parser.create()
        .withResourceBundle(ResourceBundle.getBundle("MyBundle"))
        .parseOrExit(argv);
````

The bundle keys must then be manually defined on the parameter methods:

````java
@Parameter(value = "url",
           bundleKey = "headers")
abstract String headers();
````

See [jbock-map-example](https://github.com/h908714124/jbock-map-example) for further details.


### Parsing failure

There are several types of "bad input" which can cause the parsing process to fail:

* Repetition of <a href="#repeatable-parameters">*non-repeatable*</a> parameters
* Absence of required parameter
* Too many <a href="#positional-parameters">*positional*</a> parameters
* Missing value of <a href="#binding-parameters">*binding*</a> parameter
* Any `RuntimeException` in a mapper or collector

The generated `parseOrExit` method performs the following steps if such a failure is encountered:

* Print an error message to the configured <a href="#runtime-modifiers">*error stream*</a>
* Shut down the JVM with the configured <a href="#runtime-modifiers">*error code*</a>

If you need to handle the error-case manually,
you can use the generated `parse` method.
This method returns a "union type" to signal parsing success or error,
and doesn't have side effects, like printing to `System.out` or shutting down the jvm.

### Runtime modifiers

The output streams, as well as some other parameters can be changed before one of the parse methods is invoked.
This example shows all the available options:

````java
String[] argv = {"-f hello.txt"};
MyArguments_Parser.create()
    .withErrorExitCode(2)                                           // default is 1
    .withErrorStream(new PrintStream(new ByteArrayOutputStream()))  // used for parsind errors, default is System.err
    .withOutputStream(new PrintStream(new ByteArrayOutputStream())) // used when --help is passed, default is System.out
    .withIndent(2)                                                  // default is 7
    .withResourceBundle(ResourceBundle.getBundle("UserOpts"))       // default is none
    .parseOrExit(argv);
````

The `indent` is used when printing the usage page.

### Limitations

* No grouping. For example, `rm -rf` is invalid, use `rm -r -f` instead
* Option names always start with `"-"` or `"--"`.
* No multi-argument options. Workaround: Repeatable options.
* No bsd-style flags as in `tar xzf`, use `tar -x -z -f` instead
* Mnemonics (single-dash options) are limited to a single character.
* Cannot distinguish between attached or detached option shape. Both are always allowed and equivalent.

### Gradle config

see [jbock-gradle-example](https://github.com/h908714124/jbock-gradle-example)

### Maven config

see [jbock-maven-example](https://github.com/h908714124/jbock-maven-example)

### Running tests

````sh
./gradlew :core:clean :core:test :examples:clean :examples:test
````
