## jbock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

jbock is a simple and convenient ~~application server~~ *command line parser.*

The command line options are defined as an annotated abstract class, see example:

````java
@CommandLineArguments
abstract class MyArguments {

  @PositionalParameter
  abstract Path path();

  @Parameter(shortName = 'v')
  abstract OptionalInt verbosity();
}
````

Note that `path` is a required parameter, because its type is not `Optional`.
This type-inferred optionality, which can also be made explicit,
is easy to remember by the rule that *jbock never returns null*.

Now **build once** to trigger the code generation. See also <a href="#gradle-config">*gradle*</a> or
<a href="#maven-config">*maven*</a> config.

The derived class `MyArguments_Parser`, the source of which has now been generated,
can be used in your `main` method as follows:

````java
String[] argv = { "-v2", "file.txt" }; // for example
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

// make sure this works as expected...
assertEquals(OptionalInt.of(2), args.verbosity());
assertEquals(Paths.get("file.txt"), args.path());
````


### Big list of bullet points

* <a href="#features-overview">Features overview</a>
* <a href="#argument-vector">Argument vector</a>
* <a href="#positional-parameters">Positional parameters</a>
* <a href="#flags">Flags</a>
* <a href="#binding-parameters">Binding parameters</a>
* <a href="#required-and-optional-parameters">Required and optional parameters</a>
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
* <a href="#examples-and-other-links">Examples and other links</a>
* <a href="#running-tests">Running tests</a>

### Features overview

Some of the features, especially the handling of optional parameters, may be unexpected
for users of similar parsers:

1. In the Java model, <a href="#required-and-optional-parameters">*optional parameters*</a>
   correspond to methods that return [Optional](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html).
   Coincidentally, there is no way to make a parameter method return `null`.
1. <a href="#binding-parameters">*Binding parameters*</a> are [unary:](https://en.wikipedia.org/wiki/Unary_operation)
    1. Each parameter name must be followed by a single argument.
       If we think of the `argv` array as a map,
       then this means that there's only one value per key.
    1. One such key-value pair can also <a href="#parameter-shapes">*appear as a single token.*</a>
    1. Parameters can be <a href="#repeatable-parameters">*repeatable.*</a>
       Repeatable parameters correspond to methods that return [List.](https://en.wikipedia.org/wiki/Java_collections_framework)
1. There are some <a href="#standard-coercions">*standard coercions.*</a>
Custom <a href="#custom-mappers-and-parameter-validation">*mappers*</a> and
<a href="#custom-collectors">*collectors*</a> can be added.

Now let's look at some features in more detail.

### Argument vector

Command line applications have access to a special array of strings,
which is often called `args` or `argv`.

Sometimes the tokens in this array are grouped into key-value pairs.
Sometimes they are distinguished by their position or form.
There can also be a mix of these kinds.
The tokens and key-value pairs in `argv` are called
*command line parameters,* or in short, *parameters.*

Let's take a look at three basic types of parameters:

1. <a href="#positional-parameters">*positional parameters*</a>
1. <a href="#flags">*flags*</a>
1. <a href="#binding-parameters">*binding parameters*</a>

### Positional parameters

A *positional* parameter is just a value, without a
preceding parameter name. The value is not allowed
to start with a "minus" character, unless the
<a href="#allowing-prefixed-tokens">*allowPrefixedTokens*</a>
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
otherwise <a href="#parsing-failure">*parsing will fail*</a>.

The `source` parameter has the *lowest* position,
so it will bind the *first positional* argument.

````java
String[] argv = { "a.txt", "b.txt" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);
assertEquals(Paths.get("a.txt"), args.source());
assertEquals(Paths.get("b.txt"), args.target());
````

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
are <a href="#binding-parameters">*free*</a> in `argv`.

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

### Binding parameters

A <a href="#positional-parameters">*non-positional*</a> parameter
that is not a <a href="#flags">*flag*</a> is called a
*binding parameter*. The following
method declares binding parameter:

````java
// example of a binding parameter
@Parameter(shortName = 'f')
abstract String file();
````

The *parameter value* (or *bound value*) is the next token after the first occurrence of the
token `-f`. The parameter value
is an *arbitrary* string. For example, it's fine for the value to start with a "minus" character:

````java
String[] argv = { "-f", "-f" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);
assertEquals("-f", args.file());
````

### Required and optional parameters

All <a href="#binding-parameters">*binding parameters*</a>
are treated as *required* unless declared otherwise.
It is not possible to get an instance
of `MyArguments` unless the input array `String[] argv`
contains all required parameters. Therefore, we can guarantee that none of your
model methods will ever return `null`.

To declare an *optional* binding parameter,
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
you can also set the `optional` attribute
in addition to that:

````java
@Parameter(shortName = 'f', optional = true)
abstract Optional<String> file();
````


### Repeatable parameters

Repeatable parameters are <a href="#binding-parameters">*binding parameters*</a>
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

Given a <a href="#binding-parameters">*binding parameter*</a> like this

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

Binding parameters can also be written in *attached* form as follows

````java
argv = { "--file=data.txt" }; // attached long form
argv = { "-fdata.txt" }; // attached short form
````

Note: If both `longName` and `shortName` are defined, there
are *four* different ways to write a binding parameter.

On the other hand, if both names are defined,
there are only *two* ways to write a <a href="#flags">*flag:*</a>

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
to write a custom mapper first. Optional and List of these
types are also allowed.

### Custom mappers and parameter validation

Mappers (a.k.a. converters) must implement [Supplier](https://docs.oracle.com/javase/8/docs/api/java/util/function/Supplier.html)`<`[Function](https://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html)`<`[String](https://docs.oracle.com/javase/8/docs/api/java/lang/String.html)`, ?>>`,
where `?` depends on the parameter it's used on.
The mapper's input is the parameter's value, taken directly from `String[] argv`.
If the parameter does not appear in `argv`, then the mapper is not invoked.
The mapper may reject its input by throwing any [RuntimeException](https://docs.oracle.com/javase/8/docs/api/java/lang/RuntimeException.html).

````java
class PositiveNumberMapper implements Supplier<Function<String, Integer>> {

  @Override
  public Function<String, Integer> get() {
    return s -> {
      // try-catch is not needed here: NumberFormatException is a RuntimeException 
      Integer i = Integer.valueOf(s);
      if (i < 0) {
        // Perform additional validation by throwing IllegalArgumentException, which is a RuntimeException
        throw new IllegalArgumentException("The value cannot be negative.");
      }
      return i;
    };
  }
}
````

The same mapper can also be used for
<a href="#required-and-optional-parameters">*optional*</a>
and <a href="#repeatable-parameters">*repeatable*</a> parameters.
In this case, the corresponding attribute (`optional` or `repeatable`)
must be set explicitly.

````java
@Parameter(shortName = 'o', mappedBy = PositiveNumberMapper.class, optional = true)
abstract Optional<Integer> optionalNumber();
````

````java
@Parameter(shortName = 'x', mappedBy = PositiveNumberMapper.class, repeatable = true)
abstract List<Integer> numbers();
````

*Note: The mapper class must have a no-argument constructor.*

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

The mapper splits tokens of the form `a:b` into map entries,
which is expressed as a certain function from
`String` to `Entry<String, String>`:

````java
class MapTokenizer implements Supplier<Function<String, Map.Entry<String, String>>> {

  @Override
  public Function<String, Map.Entry<String, String>> get() {
    return s -> {
      String[] tokens = s.split(":", 2);
      if (tokens.length < 2) {
        throw new IllegalArgumentException("Invalid pair: " + s);
      }
      return new AbstractMap.SimpleImmutableEntry<>(tokens[0], tokens[1]);
    };
  }
}
````

The collector class must be a `Supplier<`[Collector](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collector.html)`<A, ?, B>>`
where `A` is the output of the mapper, and `B` is the
parameter type.

This class may have type variables for better reusability.

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

As mentioned before, if a parameter declares `mappedBy` or `collectedBy`, then the 
attributes `repeatable` and `optional` are not inferred from the parameter type.
They must be set explicitly.

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
@Parameter(longName = "url",
           bundleKey = "headers")
abstract String headers();
````

See [jbock-map-example](https://github.com/h908714124/jbock-map-example) for further details.

### Escape sequence

There can sometimes be ambiguity between
<a href="#positional-parameters">*positional*</a>
and <a href="#binding-parameters">*binding*</a> parameters. If the `allowEscapeSequence`
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

By default, any <a href="#binding-parameters">unbound token</a> that begins with
[hyphen-minus](https://en.wikipedia.org/wiki/Hyphen-minus) 
and is not one of the defined parameter names,
will cause a <a href="#parsing-failure">*parsing failure*</a>.
No attempt is made to interpret it as a positional argument.

Use `allowPrefixedTokens` to change this and allow the user
to pass, for instance, a negative number,
without forcing them to type the
<a href="#escape-sequence">*escape sequence*</a>:

````java
@CommandLineArguments(allowPrefixedTokens = true)
abstract class MyArguments {
  
  @PositionalParameter
  abstract int number();
}
````
For example, [negative one](https://en.wikipedia.org/wiki/%E2%88%921) can now be passed as a positional parameter.

````java
String[] argv = { "-1" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

assertEquals(-1, args.number());
````

### Parsing failure

There are several types of "bad input" which can cause the parsing process to fail:

* Repetition of <a href="#repeatable-parameters">*non-repeatable parameters*</a>
* Absence of a <a href="#required-and-optional-parameters">*required parameter*</a>
* Unknown token, after all <a href="#positional-parameters">*positional parameters*</a> are filled
* Missing value after a <a href="#binding-parameters">*binding*</a> parameter
* <a href="#standard-coercions">*Coercion*</a> failure
* Failure in a <a href="#custom-mappers-and-parameter-validation">*mapper*</a> or
  <a href="#custom-collectors">*collector*</a>

The generated `parseOrExit` method performs the following steps if such a failure is encountered:

* Print an error message to the configured <a href="#runtime-modifiers">*error stream*</a>
* Shut down the JVM with the configured <a href="#runtime-modifiers">*error code*</a>

If you need to handle the cases manually,
you should use the generated `parse` method,
which returns `ParseResult`.

````java
MyArguments_Parser.ParseResult parseResult = MyArguments_Parser.create().parse(argv);
if (parseResult instanceof MyArguments_Parser.ParsingFailed) {
  System.out.println("Parsing failed: " + ((MyArguments_Parser.ParsingFailed) parseResult).message());
  System.out.println("Usage info has been printed to the output stream.");
  System.exit(1);
} else if (parseResult instanceof MyArguments_Parser.HelpPrinted) {
  System.out.println("The user has passed the --help param.");
  System.out.println("Usage info has been printed to the error stream.");
} else if (parseResult instanceof MyArguments_Parser.ParsingSuccess) {
  MyArguments args = ((MyArguments_Parser.ParsingSuccess) parseResult).result();
  runTheBusinessLogicAlready(args);
}
````

Let's hope that this awkward case-handling gets nicer when java finally gets *sealed types.*

### Runtime modifiers

The output streams, as well as some other parameters can be changed before one of the parse methods is invoked.
This example shows all the available options:

````java
String[] argv = {"-f hello.txt"};
MyArguments_Parser.create()
    .withErrorExitCode(2)                                           // default is 1
    .withErrorStream(new PrintStream(new ByteArrayOutputStream()))  // default is System.err
    .withOutputStream(new PrintStream(new ByteArrayOutputStream())) // default is System.out
    .withIndent(2)                                                  // default is 7
    .withResourceBundle(ResourceBundle.getBundle("UserOpts"))       // default is none
    .parseOrExit(argv);
````

The `indent` is used when printing the usage page.

### Gradle config

Add two dependencies to `build.gradle`:

````groovy
compileOnly 'com.github.h908714124:jbock-annotations:2.2'
annotationProcessor 'com.github.h908714124:jbock:$jbockVersion'
````

Now add your model class and do `gradle build` once.
If Intellij doesn't "see" the generated classes,
try setting up a `generated` folder as follows:

````groovy
compileJava {
    options.compilerArgs << "-s"
    options.compilerArgs << "$projectDir/src/main/generated/java"

    doFirst {
        file(new File(projectDir, "/src/main/generated/java")).mkdirs()
    }
}

clean.doLast {
    file(new File(projectDir, "/src/main/generated")).deleteDir()
}

sourceSets {
    generated {
        java {
            srcDir "$projectDir/src/main/generated/java"
        }
    }
}
````

It may also be necessary to uncheck the `Create separate module per source set`
option (`Settings -> Build, Execution, Deployment -> Gradle`),
then run `gradle build` once, and mark the `src/main/generated` folder as *generated sources
root* (right click on folder icon in project view, or via module settings).

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

The processor itself is only needed on the compiler classpath:

````xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.8.1</version>
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

### Examples and other links

* [examples](https://github.com/h908714124/jbock/tree/master/examples) (part of this repository)
* [aws-glacier-multipart-upload](https://github.com/h908714124/aws-glacier-multipart-upload)
* [map example](https://github.com/h908714124/jbock-map-example) (described in <a href="#custom-collectors">*custom-collectors*</a>)
* [maven example](https://github.com/h908714124/jbock-maven-example)
* [List of standard coercions](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/helloworld/JbockAllTypes.java)

### Running tests

````sh
gradle :core:test :examples:clean :examples:test
````
