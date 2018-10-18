## jbock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

`jbock` is an annotation-driven command line parser, similar to [jcommander](http://jcommander.org/), but doesn't use reflection.
Instead, it generates custom source code through a mechanism called Java annotation processing.

### Overview

An annotated class looks like this

````java
@CommandLineArguments
abstract class MyArguments {

  @PositionalParameter
  abstract Optional<Path> path();
  
  @Parameter(optional = true, longName = "verbosity", shortName = 'v')
  abstract OptionalInt verbosity();

}
````

and then the generated class `LsArguments_Parser` can be used as follows

````java
String[] argv = { "--verbosity", "2", "file.txt" };
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

assertEquals(OptionalInt.of(2), args.verbosity());
````

### Required vs. Optional parameters

All parameters are <em>required by default</em>.
You can only get an instance of `MyArguments` if `argv` contains all
required parameters.

Optional parameters must use an optional type,
like `Optional<String>`,
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
By using a custom collector, it is also possible to collect repeatable parameters into other collections,
like `Set` or even `Map`. See example below:

````java
@Parameter(repeatable = true, 
           shortName = 'X',
           mappedBy = MapTokenizer.class,
           collectedBy = MapCollector.class)
abstract List<String> headers();
````

````java
static class MapTokenizer implements Supplier<Function<String, Map.Entry<String, String>>> {

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
static class MapCollector<K, V> implements Supplier<Collector<Map.Entry<K, V>, ?, Map<K, V>>> {

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
@Parameter(longName = 'url',
           bundleKey = "headers")
abstract String headers();

````

### Parameter validation

A custom mapper can be used for validation.
The mapper may reject a token by throwing any `RuntimeException`.

````java
import java.util.function.Function;

class PositiveNumberMapper implements Function<String, Integer> {

  @Override
  public Integer apply(String s) {
    Integer i = Integer.valueOf(s);
    if (i < 0) {
      throw new IllegalArgumentException("The value cannot be negative.");
    }
    return i;
  }
}
````
