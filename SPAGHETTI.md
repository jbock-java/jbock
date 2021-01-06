### Contents

* <a href="#user-content-introduction">Introduction</a>
* <a href="#user-content-positional-parameters">Positional parameters</a>
* <a href="#user-content-flags">Flags</a>
* <a href="#user-content-named-options">Named options</a>
* <a href="#user-content-escape-sequence">Escape sequence</a>
* <a href="#user-content-repeatable-parameters">Repeatable parameters</a>
* <a href="#user-content-parameter-shapes">Parameter shapes</a>
* <a href="#user-content-showing-help">Showing help</a>
* <a href="#user-content-standard-coercions">Standard coercions</a>
* <a href="#user-content-custom-mappers-and-parameter-validation">Custom mappers and parameter validation</a>
* <a href="#user-content-custom-collectors">Custom collectors</a>
* <a href="#user-content-parameter-descriptions-and-internationalization">Parameter descriptions and internationalization</a>
* <a href="#user-content-parsing-failure">Parsing failure</a>
* <a href="#user-content-runtime-modifiers">Runtime modifiers</a>
* <a href="#user-content-limitations">Limitations</a>
* <a href="#user-content-running-tests">Running tests</a>

### Introduction

A Java `main` Method has a parameter of type `String[]`,
which contains the "extra" command line parameters of the `java` invocation.
If the application is in an executable `.jar` file, these are any
remaining parameters after the option `-jar file.jar`.

### Positional parameters

In order to distinguish it from the options, the string that is passed as a positional parameter is not allowed
to start with a dash. The <a href="#user-content-escape-sequence">*escape sequence*</a> can be used to get around this limitation.

The following class declares two positional parameters:

````java
@Command
abstract class Cp {

  @Param(1)
  abstract Path source();
  
  @Param(2)
  abstract Path target();
}
````

The method `Cp_Parser#parseOrExit(String[])` that is generated
from this example requires an input array of length *exactly* `2`,
because neither of the two params are `Optional`.

The `source` param has the lowest position,
so it will correspond to the first token, while
`target` will correspond to the second token in the input array.

### Flags

A value-less named option is called a *flag*.
To declare a flag, simply declare a named option of type
`boolean` or `Boolean`,
with no custom mapper or collector.

````java
@Option(value = "quiet", mnemonic = 'q')
abstract boolean quiet();
````

At runtime, the method will return `true`
if either `--quiet` or `-q`
are present in the input array, and are not escaped or bound by any other options.

### Named options

````java
// example of a named option
@Option("file")
abstract String file();
````

`file` is a required option, since the option type `String` is not wrapped in `java.util.Optional`.
The *value* of this option is the token that immediately follows `--file` in the input array.
Since this named option is not a flag, the value must be present.
So `--file` cannot be the last token 
in the input array. The value can be any string. In particular, it is allowed to start with a dash.

### Escape sequence

The escape sequence consists of the free token `"--"`.
That's two consecutive dashes which are not either bound by any other named option, or already escaped themselves.
Any remaining tokens *after* the escape sequence in the input array will be treated as positional parameters,
regardless of their shape.
In other words, the escape sequence ends the parsing of named options.

If for some reason you wish to deactivate this escaping mechanism, please note that 
the generated parser will not contain the code to recognize the escape sequence if your command doesn't define any
positional parameters.

### Repeatable parameters

Both named options and positional parameters can be *repeatable*.
The following example declares a repeatable option:

````java
@Option(value = "header", mnemonic = 'H')
abstract List<String> headers();
````

This list will contain the values of all `-H` or `--header` options in the input array,
in the same order as in the input array.

To declare a repeatable named option or positional parameter,
either define a custom collector, or
use a parameter type of the form `java.util.List<?>`.

### Parameter shapes

Suppose a named option is defined as follows:

````java
@Option(value = "file", mnemonic = 'f')
abstract Path file();
````

Then in the input array, the option can be passed in these two equivalent forms:

````java
String[] argv;
argv = { "--file", "data.txt" }; // standard
argv = { "-f", "data.txt" };     // mnemonic
````

Alternatively it can be passed in two equivalent *attached* forms:

````java
argv = { "--file=data.txt" }; // attached standard
argv = { "-fdata.txt" };      // attached mnemonic
````

### Showing help

The token `--help` has a special meaning, but only if it is the first token in `argv`:

````java
String[] argv = { "--help" };
MyArguments args = new MyArguments_Parser().parseOrExit(argv);
````

Given this input, or any input array where `--help` is the first token,
`parseOrExit` will print usage information
about the declared options and parameters to the standard output,
and then shut the JVM down with an exit code of `0`
Any remaining tokens in the input array will be ignored.

The special meaning of the `--help` token can be disabled like this:
`@Command(helpDisabled = true)`. 

### Standard coercions

All non-private enums, as well as
[some standard Java types](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/helloworld/JbockAutoTypes.java)
can be used as parameter types, without having
to write a custom mapper.

### Custom mappers and parameter validation

Mappers (a.k.a. converters) must implement [Function](https://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html)`<`[String](https://docs.oracle.com/javase/8/docs/api/java/lang/String.html)`, ?>`,
or a [Supplier](https://docs.oracle.com/javase/8/docs/api/java/util/function/Supplier.html) of such a function,
where `?` depends on the option or parameter it's used on.
At runtime, the mapper is invoked once for each appearance of the option or parameter in `argv`.
The mapper may reject its input by throwing any [RuntimeException](https://docs.oracle.com/javase/8/docs/api/java/lang/RuntimeException.html).
The mapper class must be accessible at compile time, and have a no-argument constructor.

````java
class NatMapper implements Function<String, Integer> {

  @Override
  public Integer apply(String s) {
    Integer i = Integer.valueOf(s); // exceptions are fine
    if (i < 0) {
      throw new IllegalArgumentException("negative: " + i);
    }
    return i;
  }
}
````

### Custom collectors

The following example shows how a repeatable
option can be parsed into a `Map`,
by declaring a custom mapper and collector:

````java
@Option(value = "headers",
        mnemonic = 'H'
        mappedBy = MyTokenizer.class,
        collectedBy = MyCollector.class)
abstract Map<String, String> headers();
````

This mapper parses a token of the form `-Hfoo:bar` into a map entry:

````java
class MyTokenizer implements Function<String, Map.Entry<String, String>> {

  @Override
  public Map.Entry<String, String> apply(String s) {
    String[] tokens = s.split(":", 2);
    if (tokens.length < 2) {
      throw new IllegalArgumentException("Invalid token: " + s);
    }
    return new AbstractMap.SimpleImmutableEntry<>(tokens[0], tokens[1]);
  }
}
````

The collector class must implement either [Collector](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collector.html)`<A, ?, B>` or `Supplier<Collector<A, ?, B>`,
where `A` is mappable, and `B` is the option's or parameter's type.
The collector class may have type parameters, as long as they can be chosen so that `A` and `B` are matched.

````java
class MyCollector<K, V> implements Supplier<Collector<Map.Entry<K, V>, ?, Map<K, V>>> {

  @Override
  public Collector<Map.Entry<K, V>, ?, Map<K, V>> get() {
    return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
  }
}
````

This can be tested as follows

````java
String[] argv = { "-Xhorse:12", "-Xsheep:4" };
MyArguments args = new MyArguments_Parser().parseOrExit(argv);

assertEquals(2, args.headers());
assertEquals("12", args.headers().get("horse"));
assertEquals("4", args.headers().get("sheep"));
````

### Parameter descriptions and internationalization

By default, the method's Javadoc is used as the parameter description. 
Alternatively a resource bundle can be used, which overrides the
javadoc if the bundle contains a translation for the JVM's locale:

````java
MyArguments args = new MyArguments_Parser()
        .withResourceBundle(ResourceBundle.getBundle("MyBundle"))
        .parseOrExit(argv);
````

The bundle keys must then be manually defined on the parameter methods:

````java
@Param(value = 1,
       bundleKey = "headers")
abstract String headers();
````

See [jbock-map-example](https://github.com/h908714124/jbock-map-example) for further details.


### Parsing failure

There are several types of "bad input" which can cause the parsing process to fail:

* Repetition of non-repeatable parameters
* Absence of required parameter
* Too many positional parameters
* Missing value of named option
* Any `RuntimeException` in a mapper or collector

The generated `parseOrExit` method performs the following steps if such a failure is encountered:

1. Print an error message to the configured <a href="#user-content-runtime-modifiers">*error stream*</a>.
1. Shut down the JVM with the configured <a href="#user-content-runtime-modifiers">*error code*</a>.

If manual error handling is desired, the generated `parse` method can be used instead.
This method returns a "union type" to signal one of three conditions parsing success,
parsing error and help requested,
but does not have any side effects like printing or shutting down the jvm.

### Runtime modifiers

The output streams, as well as some other parameters can be changed before either `parse` or `parseOrExit` is invoked.
This example shows all available runtime modifiers:

````java
MyArguments_Parser parser = new MyArguments_Parser()
    .withErrorStream(new PrintStream(new ByteArrayOutputStream()))  // default is System.err
    .withHelpStream(new PrintStream(new ByteArrayOutputStream()))   // default is System.out
    .withIndent(2)                                                  // default is 4
    .maxLineWidth(120)                                              // default is 80
    .withResourceBundle(ResourceBundle.getBundle("UserOpts"));      // default is none
````

The `indent` and `maxLineWidth` are print settings for the help text.

### Limitations

* The dash character has special meaning. Can't use a different character than dash.
* No grouping of flags. For example, `rm -rf` and `tar xzf` are bad, use `rm -r -f` and `tar -x -z -f` instead
* A named option always has exactly one standard name, and zero or one mnemonics.
* Only `java.util.List` and `java.util.Optional` have special meaning. For example, Guava's `Optional` doesn't work.

### Running tests

````sh
./gradlew core:clean core:test examples:clean examples:test
````

