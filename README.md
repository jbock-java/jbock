## jbock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

jbock is an annotation-driven command line parser, implemented as a compile-time annotation processor.

How does it compare to
[other parsers?](https://stackoverflow.com/questions/1524661/the-best-cli-parser-for-java)

1. In the Java model, [Optional](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html) must be used for each non-required parameter.
1. All <a href="binding-parameters">binding parameters</a> are unary. Parameters with higher arity are not supported.
1. There are standard coercions, including numbers and dates. It is also possible to register custom converters.

### Contents

* <a href="#quick-overview">Quick Overview</a>
* <a href="#parameters">Parameters</a>
* <a href="#flags">Flags</a>
* <a href="#positional-parameters">Positional parameters</a>
* <a href="#binding-parameters">Binding parameters</a>
* <a href="#repeatable-parameters">Repeatable parameters</a>
* <a href="#parameter-shapes">Parameter shapes</a>
* <a href="#showing-help">Showing help</a>
* <a href="#standard-coercions">Standard coercions</a>
* <a href="#custom-mappers-and-parameter-validation">Custom
  mappers and parameter validation</a>
* <a href="#custom-collectors">Custom collectors</a>
* <a href="#parameter-descriptions-and-internationalization">Parameter
  descriptions and internationalization</a>
* <a href="#escape-sequence">Escape sequence</a>
* <a href="#allowing-prefixed-tokens">Allowing prefixed tokens</a>
* <a href="#parsing-failure">Parsing failure</a>
* <a href="#runtime-modifiers">Runtime modifiers</a>
* <a href="#gradle-config">Gradle config</a>
* <a href="#maven-config">Maven config</a>
* <a href="#sample-projects">Sample projects</a>
* <a href="#running-tests">Running tests</a>

### Quick Overview

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
assertEquals(Paths.get("file.txt"), args.path());
````

### Parameters

Command line applications have access to an array `String[] argv`,
the contents of which are usually examined at the start of the program.
The tokens in this array, or sometimes certain pairs of tokens,
are called *parameters*.

Let's take a look at the three basic types of parameters:
<a href="#flags">flags,</a>
<a href="#positional-parameters">positional parameters</a> and
<a href="#binding-parameters">binding parameters</a>.

### Flags

These are the simplest non-positional parameters. 

To declare a flag, simply
make the parameter's corresponding model method return
`boolean` or `Boolean`.

````java
@Parameter(shortName = 'q', longName = "quiet")
abstract boolean quiet();
````

At runtime, the flag parameter's model method will return `true`
if its *short name* or *long name*
appear in `argv`.

````java
MyArguments args = MyArguments_Parser.create().parseOrExit(new String[]{ "-q" });
assertTrue(args.quiet());
args = MyArguments_Parser.create().parseOrExit(new String[]{ "--quiet" });
assertTrue(args.quiet());
````

If you want, you can also be more
be more explicit by setting the
`flag` attribute:

````java
@Parameter(shortName = 'q', flag = true)
abstract boolean quiet();
````

### Positional parameters

A *positional* parameter is just a value, without a
preceding parameter name. The value is not allowed
to start with a hyphen character, unless the
<a href="#allowing-prefixed-tokens">allowPrefixedTokens</a>
attribute is set.

````java
@CommandLineArguments
abstract class MyArguments {

  @PositionalParameter(position = 0)
  abstract Path source();
  
  @PositionalParameter(position = 1)
  abstract Path target();
}
````

If we generate the parser `MyArguments_Parser`
from this example, then `argv` must have length `2`,
otherwise <a href="#parsing-failure">parsing will fail</a>.

The `source` parameter has the *lowest* position,
so it will bind the *first* argument.

````java
String[] argv = { "a.txt", "b.txt" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);
assertEquals(Paths.get("a.txt"), args.source());
assertEquals(Paths.get("b.txt"), args.target());
````

### Binding parameters

A <a href="#positional-parameters">non-positional</a> parameter that is not a <a href="#flags">flag</a> is called a
*binding parameter*. For example, the following
model method declares a *required* parameter.

````java
// a required parameter
@Parameter(shortName = 'f')
abstract String file();
````

It is not possible to get an instance
of your model class unless `argv` contains all required parameters.

To declare an *optional* parameter,
simply make the corresponding model method return
one of these four types:
[Optional](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html),
[OptionalInt](https://docs.oracle.com/javase/8/docs/api/java/util/OptionalInt.html),
[OptionalLong](https://docs.oracle.com/javase/8/docs/api/java/util/OptionalLong.html) or
[OptionalDouble](https://docs.oracle.com/javase/8/docs/api/java/util/OptionalDouble.html).

````java
// an optional parameter
@Parameter(shortName = 'f')
abstract Optional<String> file();
````

If you want to be more explicit,
you can also set the `optional` flag
in addition to that:

````java
@Parameter(shortName = 'f', optional = true)
abstract Optional<String> file();
````

Binding parameters can be also be passed in *attached*
form; see <a href="#parameter-shapes">parameter shapes.</a>

### Repeatable parameters

Repeatable parameters are <a href="#binding-parameters">binding parameters</a>
that can appear not only once, but any number of times.

````java
@Parameter(shortName = 'X')
abstract List<String> headers();
````

The list will contain the headers in the same order
in which they appear in `argv`.

````java
String[] argv = { "-X", "Content-Type: application/json", 
                  "-X", "Content-Length: 200" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);
assertEquals(List.of("Content-Type: application/json", 
                     "Content-Length: 200"), args.headers());
````

To declare a repeatable parameter, simply
make the corresponding model method return a `List`.

You can also be more explicit by setting the
`repeatable` attribute:

````java
@Parameter(shortName = 'X', repeatable = true)
abstract List<String> headers();
````

### Parameter shapes

Given a <a href="#binding-parameters">binding parameter</a> like this

````java
@Parameter(longName = "file", shortName = 'f')
abstract Path file();
````

then we have the short and long forms, which are equivalent

````java
String[] argv;
argv = { "--file", "data.txt" }; // two hypens -> long form
argv = { "-f", "data.txt" }; // one hyphen -> short form
````

Both can also be written in *attached* form as follows

````java
argv = { "--file=data.txt" }; // attached long form
argv = { "-fdata.txt" }; // attached short form
````

Thus if both `longName` and `shortName` are defined, there
are at most *four* different ways to write a binding parameter.

For a <a href="#flags">flag,</a> there are at most two ways:

````java
argv = { "--quiet" }; // two hyphens -> long flag
argv = { "-q" }; // one hyphen -> short flag
````

### Showing help

By default, the token `--help` has a special meaning. 

````java
String[] argv = { "--help" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);
````

This time, `parseOrExit` will shut down the JVM with an exit code of `0`, and print
usage information to standard out.

This will only happen if `--help` is the *first*
tokens of the input array. Then the remaining tokens, if any,
are ignored.

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

### Custom collectors

By using a custom collector, it is possible to create a
`Set`, or `Map` or other collections. The following example
builds a `Map`:

````java
@Parameter(repeatable = true, 
           shortName = 'X',
           mappedBy = MapTokenizer.class,
           collectedBy = MapCollector.class)
abstract Map<String, String> headers();
````

As before, the mapper class is a `Supplier<Function<...>>`:

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

The collector class must be a `Supplier<Collector<...>>`.
This class can have type variables for better reusability.

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


*Note: If a parameter declares a custom mapper or collector, then the 
attributes `repeatable` and `optional` are not inferred from the parameter type.
They must be set explicitly.*

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

There can sometimes be ambiguity between
<a href="#positional-parameters">positional</a>
and regular parameters. If the `allowEscapeSequence`
attribute is present, the special token `--` can be used to resolve this.

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
assertEquals(Paths.get("-q"), args.file());
````

### Allowing prefixed tokens

By default, any token that begins with
[hyphen-minus](https://en.wikipedia.org/wiki/Hyphen-minus) 
and is not one of the defined parameter names,
will cause a <a href="#parsing-failure">parsing failure</a>.
No attempt is made to interpret it as a positional argument.

Use `allowPrefixedTokens` to change this and allow the user
to pass, for instance, a negative number,
without forcing them to type the
<a href="#escape-sequence">escape sequence</a>:

````java
@CommandLineArguments(allowPrefixedTokens = true)
abstract class MyArguments {
  
  @PositionalParameter
  abstract int number();
}
````
For example, `-1` can now be passed as a positional parameter.

````java
String[] argv = { "-1" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

assertEquals(-1, args.number());
````

### Parsing failure

There are several ways in which the user input can be wrong:

* Repetition of <a href="#repeatable-parameters">non-repeatable parameters</a>
* Absence of a required parameter
* Unknown token
* Missing value of <a href="#binding-parameters">binding parameter</a>
* Coercion failed

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

### Gradle config

This is the basic configuration for projects using the `java` plugin

````groovy
dependencies {
    compileOnly 'com.github.h908714124:jbock-annotations:2.2'
    annotationProcessor 'com.github.h908714124:jbock:$jbockVersion'
}
````

For `java-library` projects, `implementation` scope must be used for the annotations:

````groovy
dependencies {
    implementation 'com.github.h908714124:jbock-annotations:2.2'
    annotationProcessor 'com.github.h908714124:jbock:$jbockVersion'
}
````

If Intellij doesn't "see" the generated classes,
try setting up a `generated` folder as follows: 

````groovy
compileJava {
    options.compilerArgs << "-s"
    options.compilerArgs << "$projectDir/generated/java"

    doFirst {
        // make sure that directory exists
        file(new File(projectDir, "/generated/java")).mkdirs()
    }
}

clean.doLast {
    // clean-up directory when necessary
    file(new File(projectDir, "/generated")).deleteDir()
}

sourceSets {
    generated {
        java {
            srcDir "$projectDir/generated/java"
        }
    }
}
````

### Maven config

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
* [map example (described above)](https://github.com/h908714124/jbock-gradle-example)
* [maven example](https://github.com/h908714124/CopyFile)

### Running tests

````sh
gradle :core:test
gradle :examples:test
````
