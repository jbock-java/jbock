# jbock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

jbock is a simple annotation processor that generates a 
[getopt_long](https://www.gnu.org/software/libc/manual/html_node/Getopt.html)-inspired
CLI parser, with an extra focus on positional parameters.

The primary goals are:
 
* Generate concise parser code from a command line interface that's declared via annotations.
* Give the end user clear feedback via exceptions, if the input is invalid.
* Generate usage text that looks similar to a GNU man page, including SYNOPSIS. 

### User feedback is hard!

We're going to write a simple command line utility that copies a file.

````java
public class CopyFile {

  public static void main(String[] args) throws IOException {
    String src = args[0];
    String dest = args[1];
    Files.copy(Paths.get(src), Paths.get(dest));
    System.out.printf("Done copying %s to %s%n", src, dest);
  }
}
````

This is what the program prints, when invoked without parameters:

<pre><code>
Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: 0
    at cli.tools.CopyFile.main(CopyFile.java:10)
</code></pre>

### Excuse me?

We will now use jbock to give the user a human readable error message.
We define an abstract class `Args`:

````java
public class CopyFile {

  @CommandLineArguments
  static abstract class Args {
    @Positional abstract String src();
    @Positional abstract String dest();
  }

  public static void main(String[] args) throws IOException {
    Args a = CopyFile_Args_Parser.parse(args);
    Files.copy(Paths.get(a.src()), Paths.get(a.dest()));
    System.out.printf("Done copying %s to %s%n", a.src(), a.dest());
  }
}
````

The order of the method declarations `src()` and `dest()` matters.
`CopyFile_Args_Parser` is a generated class, a custom parser for the command line interface
defined by `Args`.

Now the error looks like this:

<pre><code>
Exception in thread "main" java.lang.IllegalArgumentException: Missing positional parameter: SRC
	at cli.tools.CopyFile_Arguments_Parser$Helper.extractPositionalRequired(CopyFile_Arguments_Parser.java:85)
	at cli.tools.CopyFile_Arguments_Parser$Helper.build(CopyFile_Arguments_Parser.java:77)
	at cli.tools.CopyFile_Arguments_Parser.parse(CopyFile_Arguments_Parser.java:43)
	at cli.tools.CopyFile_Arguments_Parser.parse(CopyFile_Arguments_Parser.java:23)
	at cli.tools.CopyFile.main(CopyFile.java:18)
</code></pre>







## Parser features

* <em>Short args</em>, attached `-n1` or detached `-n 1`
* <em>Long args</em>, attached `--num=1` or detached `--num 1`
* <em>Flags</em>: Short `-r` or long `--recursive`
* <em>Option grouping</em>: For example, `-xzf d.tgz` is equivalent to `-x -z -f d.tgz`. Note that the leading hyphen cannot be omitted.
* <em>Unnamed arguments</em>, like in `rm foo.txt` (see <a href="#example-curl">Example: curl</a>)
* <em>End of option scanning</em>, like in `rm -- -f` (see <a href="#example-rm">Example: rm</a>)

## Basic use

Annotate an `abstract` class with `@CommandLineArguments`.
In this class, each `abstract` method must have an empty argument list.
The method must also return one of four permissible types:

* A method that returns `List<String>` declares a <em>repeatable</em> argument that may appear any number of times.
* A method that returns `Optional<String>` declares an <em>optional</em> argument that may appear at most once.
* A method that returns `String` declares a <em>required</em> argument that must appear exactly once.
* A method that returns `boolean` declares a <em>flag</em>.

The abstract methods may also carry some of jbock's additional annotations.
These are: `@LongName`, `@ShortName`, `@SuppressLongName`, `@Description`, `@OtherTokens`, and `@EverythingAfter`.
See [here](additional_rules.md) for more details.

## Example: `curl`

This example shows the use of the `@OtherTokens` annotation.

````java
@CommandLineArguments
abstract class CurlArguments {

  @ShortName('X')
  @LongName("request")
  @Description("Optional<String> for regular arguments")
  abstract Optional<String> method();

  @ShortName('H')
  @SuppressLongName
  @Description("List<String> for repeatable arguments")
  abstract List<String> headers();

  @ShortName('v')
  @SuppressLongName
  @Description("boolean for flags")
  abstract boolean verbose();

  @OtherTokens
  @Description({
      "@OtherTokens to capture any 'other' tokens in the input.",
      "In this case, that's any token which doesn't match one of",
      "/-v/, /-X(=.*)?/, /--request(=.*)?/, or /-H(=.*)?/,",
      "or follows immediately after the equality-less version",
      "of one of the latter 3.",
      "If there were no method with the @OtherTokens annotation,",
      "such a token would cause an IllegalArgumentException to be",
      "thrown from the CurlArguments_Parser.parse method."})
  abstract List<String> urls();
}
````

`CurlArguments` carries the `@CommandLineArguments` annotation.
Therefore, a class called `CurlArguments_Parser` will be generated in the same package.

The generated static method `CurlArguments_Parser.parse(String[] args)` 
takes the `args` argument from your `public static void main`,
and returns an implementation of `CurlArguments`.
It will throw `IllegalArgumentException` if the input is invalid.
For example, `args = {"-X", "GET", "-X", "POST"}` would be invalid, 
because the "method" argument isn't repeatable: 
`method()` returns an `Optional<String>`, not a `List<String>`.

The generated static method `CurlArguments_Parser.printUsage(PrintStream out, int indent)`
prints formatted documentation about `CurlArguments` to `out`.

The generated enum `CurlArguments_Parser.Option` contains the constants `METHOD`, `HEADERS`, `VERBOSE` and `URLS`.
These correspond to the abstract methods in `CurlArguments`,
and can be used as an alternative to `printUsage`,
for more fine-grained control over the usage text.

Click [here](curl_parser_examples.md) to see how `CurlArguments_Parser` would handle some example input.

## Failure

jbock's `parse` method will throw an `IllegalArgumentException`:

* &#x2026;if multiple values are given for a non-repeatable argument.
* &#x2026;if the argument list ends after an option name.
* &#x2026;if a required option is missing.
* &#x2026;if an unknown token is encountered, and there's no <a href="#example-curl">OtherTokens</a> method.

See 
<a href="https://github.com/h908714124/jbock/blob/master/examples/src/test/java/net/zerobuilder/examples/gradle/CurlArgumentsTest.java">
this unit test</a> for more examples of the parsing behaviour.

## Example: `rm`

This example shows how to use the `@EverythingAfter` annotation.
It is used to define a special token that stops option parsing, when encountered as an unbound token.

As with the `@OtherTokens` annotation, its target method must return `List<String>`,
and at most one method can have this annotation.

Many command line tools, such as <a href="https://linux.die.net/man/1/rm">rm</a>,
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

## Maven config

No part of the jbock jar is needed at runtime, so it's enough to add it as a provided scope dependency.
The compiler plugin may also need some configuration.

````xml
<properties>
  <jbock.version>2.2.4</jbock.version>
</properties>
<dependencies>
  <dependency>
    <groupId>com.github.h908714124</groupId>
    <artifactId>jbock</artifactId>
    <version>${jbock.version}</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.7.0</version>
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

<em>Note:</em> By default, maven will put all generated sources in the folder `target/generated-sources/annotations`.

## Java 9 config

The [example project](https://github.com/h908714124/jbock/tree/master/examples) uses Java 9.
In order to use jbock on the module path, add the following to `module-info.java`:

````java
requires net.jbock;
````
