## jbock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

`jbock` is an annotation-driven command line parser, similar to 
[jcommander](http://jcommander.org/), but doesn't use reflection.
Instead, it generates custom source code 
through a mechanism called <em>annotation processing</em>,
which is essentially Java's version of <em>compile-time macros</em>.

Jbock's parameters are required by default,
and it [enforces the use of optional types](https://github.com/h908714124/jbock/tree/master/OPTIONALS.md)
for optional parameters.

### Overview

An annotated class looks like this

````java
@CommandLineArguments
abstract class MyArguments {

  @PositionalParameter(optional = true)
  abstract Optional<Path> path();
  
  @Parameter(optional = true, longName = "verbosity", shortName = 'v')
  abstract OptionalInt verbosity();
}
````

and then the generated class `MyArguments_Parser` can be used as follows

````java
String[] argv = { "--verbosity", "2", "file.txt" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

assertEquals(OptionalInt.of(2), args.verbosity());
assertEquals(Optional.of(Paths.get("file.txt")), args.path());
````

### Required vs. Optional parameters

All parameters except <em>flags</em> and
<em>repeatables</em> are, by default, required.
You can only get an instance of `MyArguments` if `argv` contains all
required parameters.

To declare an optional parameter, one must use 
an optional type, like `Optional<String>`,
and set `optional = true`.

### Flags

Flags are parameters that don't take an argument.

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

### Standard types

All enums, as well as some [standard types](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/helloworld/JbockAllTypes.java)
are supported out of the box. These can be used without having
to write a custom mapper.

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

### Parameter descriptions

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

If there is a bundle, and a key is defined and is
contained in the bundle, then that overrides the method's javadoc.

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
