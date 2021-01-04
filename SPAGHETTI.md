### Contents

* <a href="#optionparam-kinds">Option/param kinds</a>
* <a href="#positional-parameters">Positional parameters</a>
* <a href="#flags">Flags</a>
* <a href="#named-options">Named options</a>
* <a href="#escape-sequence">Escape sequence</a>f
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

### Option/Param kinds

A Java `main` Method has a parameter of type `String[]`,
which contains the "extra" command line parameters of the `java` invocation,
after the positional parameter `class` or the named option `-jar file.jar`.

### Positional parameters

A positional parameter is just an arbitrary token in `argv`, without a
preceding parameter name.
The token is not allowed to start with a dash, unless the
<a href="#escape-sequence">*escape sequence*</a> was used.
Here is an example:

````java
@Command
abstract class MyArguments {

  @Param(1)
  abstract Path source();
  
  @Param(2)
  abstract Path target();
}
````

The class `MyArguments_Parser` that is generated
from this example requires an `argv` of length *exactly* `2`,
because none of the params are `Optional`.

The `source` param has the lowest position,
so it will bind the first token, while
`target` will bind to the second token.

````java
String[] args = { "a.txt", "b.txt" };
MyArguments my = new MyArguments_Parser().parseOrExit(args);
assertEquals(Paths.get("a.txt"), my.source());
assertEquals(Paths.get("b.txt"), my.target());
````

### Flags

To declare a *flag* (which is a parameterless named option),
simply declare a named option of type
`boolean` or `Boolean`,
and don't declare a custom mapper or collector.

````java
@Option(value = "quiet", mnemonic = 'q')
abstract boolean quiet();
````

At runtime, the method will return `true`
if either `--quiet` or `-q`
are <a href="#named-options">*free*</a> in `argv`.

````java
MyArguments_Parser parser = new MyArguments_Parser();
assertTrue(parser.parseOrExit(new String[]{ "-q" }).quiet());
assertTrue(parser.parseOrExit(new String[]{ "--quiet" }).quiet());
````

### Named options

````java
// example of a named option
@Option("file")
abstract String file();
````

The token that follows `--file` can be any string; it may even start with a dash.
Any token in `argv` that is not bound by some named option, and precedes the
<a href="#escape-sequence">*escape sequence*</a>, is called *free*.

### Escape sequence

The escape sequence consists of the <a href="#named-options">*free*</a> token `"--"`,
i.e. two consecutive dashes. 
Any remaining tokens in `argv` after that will be treated as <a href="#params">*params*</a>.
In other words, the escape sequence *ends option parsing*.
The generated parser will always recognize the escape sequence,
as long as there is at least one *param* defined.

### Repeatable parameters

Both named options and positional parameters can be *repeatable*.

````java
@Option(value = "header", mnemonic = 'H')
abstract List<String> headers();
````

This list will contain headers in the same order
in which they appear in `argv`.

To declare a repeatable named option or positional parameter,
either define a custom collector, or
use a parameter type of the form `java.util.List<?>`.

### Parameter shapes

Given a named option like this

````java
@Option(value = "file", mnemonic = 'f')
abstract Path file();
````

then we have the long form and the mnemonic, which are equivalent

````java
String[] argv;
argv = { "--file", "data.txt" }; // two dashes -> long form
argv = { "-f", "data.txt" };     // single dash -> mnemonic
````

Named options can also be written in *attached* form as follows

````java
argv = { "--file=data.txt" }; // attached long form
argv = { "-fdata.txt" };      // attached mnemonic
````

On the other hand,
there are at most two ways to write a <a href="#flags">*flag:*</a>

````java
argv = { "--quiet" }; // two dashes  -> long form
argv = { "-q" };      // single dash -> mnemonic
````

### Showing help

The token `--help` has a special meaning, but only if it is the first token in `argv`! 

````java
String[] argv = { "--help" };
MyArguments args = new MyArguments_Parser().parseOrExit(argv);
````

Given this input, or any array where `--help` is the first token,
`parseOrExit` will print usage information
about the declared options and parameters to the standard output,
and then shut the JVM down with an exit code of `0`

The special meaning of the `--help` token can be disabled like this:
`@Command(helpDisabled = true)`. 

### Standard coercions

All non-private enums, as well as
[some standard Java types](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/helloworld/JbockAutoTypes.java)
can be used as parameter types, without having
to write a custom mapper first. `java.util.Optional` and `java.util.List` of these
types are also allowed.

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

1. Print an error message to the configured <a href="#runtime-modifiers">*error stream*</a>.
1. Shut down the JVM with the configured <a href="#runtime-modifiers">*error code*</a>.

If manual error handling is desired, the generated `parse` method can be used instead.
This method returns a "union type" to signal one of three conditions parsing success,
parsing error and help requested,
but does not have any side effects like printing or shutting down the jvm.

### Runtime modifiers

The output streams, as well as some other parameters can be changed before one of the parse methods is invoked.
This example shows all the available options:

````java
MyArguments_Parser parser = new MyArguments_Parser()
    .withErrorStream(new PrintStream(new ByteArrayOutputStream()))  // default is System.err
    .withHelpStream(new PrintStream(new ByteArrayOutputStream()))   // default is System.out
    .withIndent(2)                                                  // default is 4
    .maxLineWidth(120)                                              // default is 80
    .withResourceBundle(ResourceBundle.getBundle("UserOpts"))       // default is none
    ;
````

The `indent` and `maxLineWidth` are print settings for the help text.

### Limitations

* No multi-valued options or params. Workaround: Declare the option or param *repeatable*, either by making it a `List`, or defining a <a href="#custom-collectors">*custom collector.*</a>
* Option names start with two dashes. Only single-character names may use a single dash; these are called mnemonics.
* No grouping. For example, `rm -rf` and `tar xzf` are bad, use `rm -r -f` and `tar -x -z -f` instead
* An option can't have more than one long name or more than one mnemonic.
* Mappers don't currently know about option form (long name or mnemonic) or shape (attached or detached). Also it's currently not possible to forbid one of the shapes.
* Type matching currently uses `java.util.List` and `java.util.Optional` exclusively. Alternatives like `com.google.common.base.Optional` don't get special semantics.

### Gradle config

see [jbock-gradle-example](https://github.com/h908714124/jbock-gradle-example)

### Maven config

see [jbock-maven-example](https://github.com/h908714124/jbock-maven-example)

### Running tests

````sh
./gradlew core:clean core:test examples:clean examples:test
````

