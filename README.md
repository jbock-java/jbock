## jbock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

What might distinguish it from
[other parsers](https://stackoverflow.com/questions/1524661/the-best-cli-parser-for-java):

1. By default, all parameters are treated as required, except flags.
1. In the Java model, [Optional](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html) must be used for each non-required parameter.
1. Parameters are unary, except flags. Parameters with higher arity are not supported.
1. There are some standard coercions, including numbers and dates. It is also possible to register custom converters.

### Overview

For example, here's a java model:
````java
@CommandLineArguments
abstract class MyArguments {

  @PositionalParameter
  abstract Path path();
  
  @Parameter(optional = true, longName = "verbosity", shortName = 'v')
  abstract OptionalInt verbosity();
}
````

and the derived `MyArguments_Parser` can be used as follows

````java
String[] argv = { "--verbosity", "2", "file.txt" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

assertEquals(OptionalInt.of(2), args.verbosity());
assertEquals(Optional.of(Paths.get("file.txt")), args.path());
````

### Required vs. Optional parameters

All non-repeatable parameters, except flags, are treated as *required*
unless specified otherwise.
You can only get an instance of your model if
the input array contains all required parameters.

To declare an optional parameter, use
an *optional type*,
*and* set `optional = true`:

````java
@Parameter(optional = true)
abstract OptionalInt verbosity();
````

The optional types are:
[Optional](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html),
[OptionalInt](https://docs.oracle.com/javase/8/docs/api/java/util/OptionalInt.html),
[OptionalLong](https://docs.oracle.com/javase/8/docs/api/java/util/OptionalLong.html) and
[OptionalDouble](https://docs.oracle.com/javase/8/docs/api/java/util/OptionalDouble.html).

### Nullary parameters

The nullary parameters that don't take an argument are
called flags.
Their type must be `boolean`.
Their value will be `true` if one of their names
appears on the command line.

````java
@Parameter(flag = true, shortName = 'q')
abstract boolean quiet();
````

````java
String[] argv = { "-q" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

assertTrue(args.quiet());
````

### Showing help

By default, the token `--help` has a special meaning. 

````java
String[] argv = { "--help" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);
````

This will shutdown the JVM with an exit code of `0`, and print
usage information to standard out.

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


### Repeatable parameters

Repeatable parameters can appear several times in `argv`.

````java
@Parameter(repeatable = true, 
           shortName = 'X')
abstract List<String> headers();
````

````java
String[] argv = { "-X", "Content-Type: application/json", "-X", "Content-Length: 200" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

assertEquals(List.of("Content-Type: application/json", "Content-Length: 200"), args.headers());
````

By default, a repeatable parameter must be a `List`.
By using a custom mapper and collector, it is also possible to collect repeatable parameters into other collections,
like `Set` or even `Map`. See example below:

````java
@Parameter(repeatable = true, 
           shortName = 'X',
           mappedBy = MapTokenizer.class,
           collectedBy = MapCollector.class)
abstract Map<String, String> headers();
````

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

````java
class MapCollector<K, V> implements Supplier<Collector<Map.Entry<K, V>, ?, Map<K, V>>> {

  @Override
  public Collector<Map.Entry<K, V>, ?, Map<K, V>> get() {
    return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
  }
}
````

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
flag is present, the special token
`--` can be used to resolve this.

````java
@CommandLineArguments(allowEscapeSequence = true)
abstract class MyArguments {
  
  @PositionalParameter
  abstract List<Path> files();
  
  @Parameter(shortName = '-q', flag = true)
  abstract boolean quiet();
}
````

The remaining tokens after `--` are always considered
positional.


````java
String[] argv = { "-q" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

assertTrue(args.quiet());
assertEquals(Collections.emptyList(), args.files());
````

````java
String[] argv = { "--", "-q" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

assertFalse(args.quiet());
assertEquals(Collections.singletonList("-q"), args.files());
````

### Prefixed tokens

Any unbound, unescaped token that begins with
[hyphen-minus](https://en.wikipedia.org/wiki/Hyphen-minus)
and isn't one of the defined parameter names, 
is rejected.
The `allowPrefixedTokens = true` flag changes this.

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

### Attached and detached parameters

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

### Positional parameters

In a way these are the opposite of <em>flags</em>:
whereas a flag is just a parameter name without a value after it,
a positional parameter is a "naked" value without a
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

````java
String[] argv = { "a.txt", "b.txt" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);
assertEquals(Paths.get("a.txt"), args.source());
assertEquals(Paths.get("b.txt"), args.target());
````

### Handling failure

There are several ways in which the user input can be wrong:

* Repetition of non-repeatable parameters
* Absence of required parameters
* Unknown token
* Missing value
* Parsing error (for example: bad number format)

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

jbock offers two different ways of handling invalid input.
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
