# jbock

jbock is a simple annotation processor that generates a [getopt_long](https://www.gnu.org/software/libc/manual/html_node/Getopt.html)-inspired
CLI parser. It can be used to define both short and long options.

jbock generates an implementation of an abstract, user-defined class.
[auto-value](https://github.com/google/auto/tree/master/value) users will be familiar with this.

## Goodies

* Defines a valid Java 9 module.
* No reflection, purely static analysis.
* No runtime dependency. The processor generates a single, self-contained class.

## Gotchas

jbock's `parse` method will throw an `IllegalArgumentException`:

* &#x2026;if multiple values are given for a non-repeatable argument.
* &#x2026;if the argument list ends after an option name.
* &#x2026;if a required option is missing.
* &#x2026;if an unknown token is encountered, and there's no <a href="#example-curl">OtherTokens</a> method.

## Parser features

* <em>Short args</em>, attached `-n1` or detached `-n 1`
* <em>Long args</em>, attached `--num=1` or detached `--num 1`
* <em>Flags</em>: Short `-r` or long `--recursive`
* <em>Parameter grouping</em>: For example, `-xzf d.tgz` is equivalent to `-x -z -f d.tgz`
* <em>Unnamed arguments</em>, like in `rm foo.txt` (see <a href="#example-curl">Example: curl</a>)
* <em>End of option scanning</em>, like in `rm -- -f` (see <a href="#example-rm">Example: rm</a>)

See 
<a href="https://github.com/h908714124/jbock/blob/master/examples/src/test/java/net/zerobuilder/examples/gradle/CurlArgumentsTest.java">
this unit test</a> for more examples of the parsing behaviour.

## Basic usage

Annotate an `abstract` class with `@CommandLineArguments`.
In this class, each `abstract` method must have an empty argument list.
Only three different return types are allowed for any such method:

* A method that returns `List<String>` declares a <em>repeatable</em> argument that may appear any number of times.
* A method that returns `Optional<String>` declares an <em>optional</em> argument that may appear at most once.
* A method that returns `String` declares a <em>required</em> argument that must appear exactly once.
* A method that returns `boolean` declares a value-less argument, a.k.a. <em>flag</em>. Flags may appear at most once.

See [here](additional_rules.md) for more details.

## Example: `curl`

````java
@CommandLineArguments
abstract class CurlArguments {

  @ShortName('X')
  @Description("Optional<String> for non-repeatable arguments")
  abstract Optional<String> method();

  @ShortName('H')
  @Description("List<String> for repeatable arguments")
  abstract List<String> headers();

  @ShortName('v')
  @Description("boolean for flags")
  abstract boolean verbose();

  @OtherTokens
  @Description({
      "@OtherTokens to capture any 'other' tokens in the input.",
      "In this case, everything that isn't a '-v' flag",
      "or follows directly after a '-H' or '-X' token.",
      "If no method carries the @OtherTokens annotation, such a",
      "token would cause IllegalArgumentException immediately."})
  abstract List<String> urls();
}
````

`@CommandLineArguments` triggers the code generation. 
A class called `CurlArguments_Parser` will be generated in the same package.

* The static method `CurlArguments_Parser.parse(String[] args)` 
  takes the `args` array from `public static void main(String[] args)`,
  and returns a corresponding implementation of `CurlArguments`.
  It will throw `IllegalArgumentException` if the input is invalid.
  For example, `args = {"-X", "GET", "-X", "POST"}` would be invalid, 
  because the "method" argument isn't repeatable: 
  `method()` returns an `Optional<String>`, not a `List<String>`.
* The static method `CurlArguments_Parser.printUsage(PrintStream out, int indent)` prints usage text
  to the PrintStream `out`.
* The enum `CurlArguments_Parser.Option` contains the constants `METHOD`, `HEADERS`, `VERBOSE` and `URLS`.
  These correspond to the abstract methods in `CurlArguments`,
  and can be used as an alternative to `printUsage`,
  for more fine-grained control over the usage text.

Click [here](curl_parser_examples.md) to see how `CurlArguments_Parser` would handle some example input.

## Example: `rm`

This example shows how to use the `@EverythingAfter` annotation.
It is used to define a special token that stops option parsing, when encountered as an unbound token.

As with the `@OtherTokens` annotation, its target method must return `List<String>`,
and at most one method can have this annotation.

Many command line tools, such as <a href="https://linux.die.net/man/1/rm"></a>,
define such a special token. To see why this is useful,
try creating a file called `-i` as follows: `echo -n >>-i`,
and then deleting this file using the `rm` command.

````java
@CommandLineArguments
abstract class RmArguments {

  @ShortName('r')
  abstract boolean recursive();

  @ShortName('f')
  abstract boolean force();

  @OtherTokens
  abstract List<String> filesToDelete();

  @EverythingAfter("--")
  @Description({
      "@EverythingAfter to create a last resort",
      "for problematic @OtherTokens.",
      "For example, when the file name is '-f'"})
  abstract List<String> moreFilesToDelete();
}
````

## The maven side

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

````xml
<dependency>
  <groupId>com.github.h908714124</groupId>
  <artifactId>jbock</artifactId>
  <version>2.2.1</version>
  <scope>provided</scope>
</dependency>
````

For Java 9 users, one more config is currently necessary until 
[MCOMPILER-310](https://issues.apache.org/jira/browse/MCOMPILER-310) is resolved:

````xml

<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.7.0</version>
  <configuration>
    <source>1.9</source>
    <target>1.9</target>

    <!-- Necessary until MCOMPILER-310 is resolved! -->
    <annotationProcessorPaths>
      <dependency>
        <groupId>com.github.h908714124</groupId>
        <artifactId>jbock</artifactId>
        <version>2.2.1</version>
      </dependency>
    </annotationProcessorPaths>

  </configuration>
</plugin>
````

## Java 9 config

The [example project](https://github.com/h908714124/jbock/tree/master/examples) uses Java 9.
In order to use jbock on the module path, add the following to `module-info.java`:

````java
requires net.jbock;
````
