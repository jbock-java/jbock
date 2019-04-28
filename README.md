## jbock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

jbock is an annotation-driven command line parser, implemented as a compile-time annotation processor.

How does it compare to
[other parsers?](https://stackoverflow.com/questions/1524661/the-best-cli-parser-for-java)

1. In the Java model, [Optional](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html) must be used for each non-required parameter.
1. All parameters are unary, except flags. Parameters with higher arity are not supported.
1. There are standard coercions, including numbers and dates. It is also possible to register custom converters.

### Contents

* <a href="#overview">Overview</a>
* <a href="#required-vs-optional-parameters">Required
  vs. Optional parameters</a>
* <a href="#flags">Flags</a>
* <a href="#showing-help">Showing help</a>
* <a href="#standard-coercions">Standard coercions</a>
* <a href="#custom-mappers-and-parameter-validation">Custom
  mappers and parameter validation</a>
* <a href="#repeatable-parameters">Repeatable parameters</a>
* <a href="#custom-collectors">Custom collectors</a>
* <a href="#parameter-descriptions-and-internationalization">Parameter
  descriptions and internationalization</a>
* <a href="#escape-sequence">Escape sequence</a>
* <a href="#prefixed-tokens">Prefixed tokens</a>
* <a href="#attached-vs-detached-short-vs-long">Attached
  vs detached, short vs long</a>
* <a href="#positional-parameters">Positional parameters</a>
* <a href="#handling-failure">Handling failure</a>
* <a href="#runtime-modifiers">Runtime modifiers</a>
* <a href="#maven-setup">Maven setup</a>
* <a href="#sample-projects">Sample projects</a>

### Overview

Here's a Java model of a command line interface:

````java
@CommandLineArguments
abstract class MyArguments {

  @PositionalParameter
  abstract Path path();
  
  @Parameter(shortName = 'v')
  abstract OptionalInt verbosity();
}
````

The derived class `MyArguments_Parser` can be used as follows

````java
String[] argv = { "-v2", "file.txt" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

assertEquals(OptionalInt.of(2), args.verbosity());
assertEquals(Optional.of(Paths.get("file.txt")), args.path());
````

### Required vs. Optional parameters

By default, a non-<a href="#repeatable-parameters">repeatable</a>
parameter definition that is
not a <a href="#flags">flag</a> is treated as *required*.
You can only get an instance of your model if
the `String[] argv` input array contains all required parameters.

To declare an *optional* parameter,
simply make the corresponding model method return
one of these four types:
[Optional](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html),
[OptionalInt](https://docs.oracle.com/javase/8/docs/api/java/util/OptionalInt.html),
[OptionalLong](https://docs.oracle.com/javase/8/docs/api/java/util/OptionalLong.html) or
[OptionalDouble](https://docs.oracle.com/javase/8/docs/api/java/util/OptionalDouble.html).

If you want to be more explicit,
you can also set the `optional` flag
in addition to that.

````java
@Parameter(shortName = 'v', optional = true)
abstract OptionalInt verbosity();
````

### Flags

The "nullary" parameters that don't take an argument are
called flags. 
They will resolve to `true`
if their short or long name
appears on the command line.

````java
@Parameter(shortName = 'q')
abstract boolean quiet();
````

````java
String[] argv = { "-q" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

assertTrue(args.quiet());
````

To declare a flag, simply
make the corresponding model method return
`boolean` or `Boolean`.

If you want, you can also be more
be more explicit by setting the
`flag` attribute:

````java
@Parameter(shortName = 'q', flag = true)
abstract boolean quiet();
````

### Showing help

By default, the token `--help` has a special meaning. 

````java
String[] argv = { "--help" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);
````

This time, `parseOrExit` will shut down the JVM with an exit code of `0`, and print
usage information to standard out.

This will only work if `--help` is the first (or only) element of the input array.

To disable the special meaning of the `--help` token, use
`@CommandLineArguments(allowHelpOption = false)`. 

### Standard coercions

All non-private enums, as well as
[some standard Java types](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/helloworld/JbockAllTypes.java)
can be used as parameter types, without having
to write a custom mapper first. Optional and Lists of these
types are allowed too.

### Custom mappers and parameter validation

Mappers must be of the type `Supplier<Function<String, ?>>`,
where `?` depends on the parameter it's used on.
The mapper may reject a token by throwing any `RuntimeException`.

````java
class PositiveNumberMapper implements Supplier<Function<String, Integer>> {

  @Override
  public Function<String, Integer> get() {
    return s -> {
      Integer i = Integer.valueOf(s);
      if (i < 0) {
        throw new IllegalArgumentException("The value cannot be negative.");
      }
      return i;
    };
  }
}
````

The same mapper can be used for required, optional and repeatable parameters:

````java
@Parameter(shortName = 'n', mappedBy = PositiveNumberMapper.class)
abstract Integer requiredNumber();
````

````java
@Parameter(shortName = 'o', mappedBy = PositiveNumberMapper.class, optional = true)
abstract Optional<Integer> optionalNumber();
````

````java
@Parameter(shortName = 'x', mappedBy = PositiveNumberMapper.class, repeatable = true)
abstract List<Integer> numbers();
````

When using a custom mapper, the attributes `repeatable` and
`optional` must be given explicitly.

### Repeatable parameters

Repeatable parameters can appear several times in `argv`.

````java
@Parameter(shortName = 'X')
abstract List<String> headers();
````

````java
String[] argv = { "-X", "Content-Type: application/json", "-X", "Content-Length: 200" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

assertEquals(List.of("Content-Type: application/json", "Content-Length: 200"), args.headers());
````

To declare a repeatable parameter, simply
make the corresponding model method return a `List`.

You can also be more explicit by setting the
`repeatable` attribute:

````java
@Parameter(shortName = 'X', repeatable = true)
abstract List<String> headers();
````

### Custom collectors

By using a custom collector, it is possible to create a
`Set`, or `Map` or other collections.

````java
@Parameter(repeatable = true, 
           shortName = 'X',
           mappedBy = MapTokenizer.class,
           collectedBy = MapCollector.class)
abstract Map<String, String> headers();
````

As before, the mapper class is a `Supplier<Function>`:

````java
class MapTokenizer implements Supplier<Function<String, Map.Entry<String, String>>> {

  @Override
  public Function<String, Map.Entry<String, String>> get() {
    return s -> {
      String[] tokens = s.split(":", 2);
      if (tokens.length < 2) {
        throw new IllegalArgumentException("Invalid pair: " + s);
      }
      return new AbstractMap.SimpleImmutableEntry<>(tokens[0].trim(), tokens[1].trim());
    };
  }
}
````

The collector class must be a `Supplier<Collector>`.
Type variables can be used for here.


````java
class MapCollector<K, V> implements Supplier<Collector<Map.Entry<K, V>, ?, Map<K, V>>> {

  @Override
  public Collector<Map.Entry<K, V>, ?, Map<K, V>> get() {
    return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
  }
}
````

When using a custom mapper or collector, the attributes
`repeatable` and `optional` must be set explicitly.

### Parameter descriptions and internationalization

By default, the method's Javadoc is used as the parameter description. 
Alternatively a resource bundle can be used:

````java
MyArguments args = MyArguments_Parser.create()
        .withResourceBundle(ResourceBundle.getBundle("MyBundle"))
        .parseOrExit(argv);
````

The bundle keys must then be manually defined on the parameter methods:

````java
@Parameter(longName = "url",
           bundleKey = "headers")
abstract String headers();
````

If a resource bundle is supplied (see above),
and the method's `bundleKey` is defined and contained in the bundle,
then the corresponding text will be used in the help page,
rather than the method's javadoc.

### Escape sequence

There can sometimes be ambiguity between positional
and regular parameters. If the `allowEscapeSequence = true`
flag is given, the special token `--` can be used to resolve this.

````java
@CommandLineArguments(allowEscapeSequence = true)
abstract class MyArguments {
  
  @PositionalParameter
  abstract Path file();
  
  @Parameter(shortName = 'q')
  abstract boolean quiet();
}
````

The remaining tokens after the escape sequence `--` are treated as positional:

````java
String[] argv = { "--", "-q" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

assertFalse(args.quiet());
assertEquals(Paths.get("-q"), args.files());
````

### Prefixed tokens

An unknown token that begins with
[hyphen-minus](https://en.wikipedia.org/wiki/Hyphen-minus) 
will cause a `RuntimeException`. No attempt is made to
interpret it as a positional argument.
Use `allowPrefixedTokens` to allow the user
to pass, for example, a negative number,
without forcing them to type the
<a href="#escape-sequence">escape sequence</a>:

````java
@CommandLineArguments(allowPrefixedTokens = true)
abstract class MyArguments {
  
  @PositionalParameter
  abstract int possiblyNegativeNumber();
}
````

````java
String[] argv = { "-1" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

assertEquals(-1, args.possiblyNegativeNumber());
````

### Attached vs detached, short vs long

Given a parameter like this

````java
@Parameter(longName = "file", shortName = 'f')
abstract Path file();
````

then the following are all equivalent:

````java
argv = { "--file", "data.txt" }; // detached long
argv = { "--file=data.txt" }; // attached long
argv = { "-f", "data.txt" }; // detached short
argv = { "-fdata.txt" }; // attached short
````

Note that the long name is always preceded by
*two* hyphen-minus characters, and the short name
is always preceded by a *single* hyphen-minus.


### Positional parameters

In a way these are dual to <a href="#flags">flags</a>.
A flag is just a parameter name without a value after it.
By contrast, a positional parameter is a "naked" value without a
preceding parameter name.

````java
@CommandLineArguments
abstract class MyArguments {

  @PositionalParameter(position = 0)
  abstract Path source();
  
  @PositionalParameter(position = 1)
  abstract Path target();
}
````

Now the order of the arguments matters,
as defined by the `position` attribute.
The `source` parameter has the *lowest* position,
so it will bind the *first* argument.

````java
String[] argv = { "a.txt", "b.txt" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);
assertEquals(Paths.get("a.txt"), args.source());
assertEquals(Paths.get("b.txt"), args.target());
````

### Handling failure

There are several ways in which the user input can be wrong:

* Repetition of <a href="#repeatable-parameters">non-repeatable parameters</a>
* Absence of required parameters
* Unknown or unbound token
* Missing value for a unary parameter
* Coercion failure

For example, let's say we have a required argument `-f`:

````java
@CommandLineArguments
abstract class MyArguments {
  @Parameter(shortName = 'f')
  abstract Path file();
}
````

then the empty array `argv = {}` would be invalid input,
because the required argument `-f` is absent.

We've seen some examples using the `parseOrExit` method before, which simply shuts down the jvm.
There's also `parse` which requires more effort, but is also more flexible:
  
````java
String[] argv = {};
MyArguments.ParseResult parseResult = MyArguments_Parser.create().parse(argv);
if (parseResult.error()) {
  System.out.println("Invalid input. This has already been printed to stderr.");
}
if (parseResult.helpPrinted()) {
  System.out.println("The user has passed the --help param. Usage info has been printed to stdout.");
}
Optional<MyArguments> result = result.result();
result.ifPresent(this::runTheBusinessLogicAlready);
````

### Runtime modifiers

The output streams, as well as some other parameters can be changed before one of the parse methods is invoked.
This example shows all the available options:

````java
String[] argv = {"-f hello.txt"};
AdditionArguments_Parser.create()
    .withErrorExitCode(2)                                           // default is 1
    .withErrorStream(new PrintStream(new ByteArrayOutputStream()))  // default is System.err
    .withOutputStream(new PrintStream(new ByteArrayOutputStream())) // default is System.out
    .withIndent(2)                                                  // default is 7
    .withResourceBundle(ResourceBundle.getBundle("UserOpts"))       // default is none
    .parseOrExit(argv);
````

The `indent` is used when printing the usage page. Note that the output, if any, is printed to
the error stream, unless `argv` is `{ "--help" }`, and `allowHelpOption` is `true`.

### Maven setup

The annotations are in a separate jar.
They are not needed at runtime, so the scope can be `optional`
or `provided`.

````xml
<dependencies>
    <dependency>
      <groupId>com.github.h908714124</groupId>
      <artifactId>jbock-annotations</artifactId>
      <version>2.2</version>
      <scope>provided</scope>
    </dependency>
</dependencies>
````

The processor itself is only needed on the compiler classpath.

````xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.8.0</version>
      <configuration>
        <annotationProcessorPaths>
          <dependency>
            <groupId>com.github.h908714124</groupId>
            <artifactId>jbock</artifactId>
            <version>${jbock.version}</version>
          </dependency>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
  </plugins>
</build>
````

There's also a gradle project in the samples.

### Sample projects

* [examples (part of this repository)](https://github.com/h908714124/jbock/tree/master/examples)
* [aws-glacier-multipart-upload](https://github.com/h908714124/aws-glacier-multipart-upload)
* [jbock-gradle-example](https://github.com/h908714124/jbock-gradle-example)
* [copy-file](https://github.com/h908714124/CopyFile)
