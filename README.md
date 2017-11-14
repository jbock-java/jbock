# jbock

jbock is a simple annotation processor that generates a [getopt_long](https://www.gnu.org/software/libc/manual/html_node/Getopt.html)-inspired
CLI parser. It can be used to define both short and long options.

Just like the default behaviour of `getopt_long`, its behaviour is not `POSIXLY_CORRECT`:
non-options do not stop option parsing, so options and non-options can be in any order.

If necessary, it is possible to define a special token, that stops option parsing when encountered.
See the `rm` example below.

### What sets it apart

* No reflection, purely static analysis.
* No runtime dependency. The generated class `*_Parser.java` is self-contained.
* Convenient, flexible property binding via abstract methods.
* Uses `Optional<String>`, not `String` for regular properties.
* Deliberately simple: No <em>converters</em>, <em>default values</em> or <em>required checking</em>.
  With Java 8 or later, it's easy to add this stuff by hand.

### Features

* Short args can be written `-n1` or `-n 1` style.
* Long args can be written `--num=1` or `--num 1` style.
* A long flag is written `--zap`, a short flag `-z`.
* Grouping is possible, as in `tar -xzf d.tgz`.
* "Non-options", like in `rm foo.txt`: Use `@OtherTokens`
* "End of option scanning", like in `rm -- foo.txt`: Use `@EverythingAfter("--")`

### Basic usage

Annotate an abstract class with `@CommandLineArguments`.
The annotation processor will consider all abstract methods that have an empty argument list.
For these methods, only three types of return types are allowed:

* A method that returns `boolean` declares a flag.
* A method that returns `List<String>` declares a repeatable argument.
* A method that returns `Optional<String>` declares an argument that may appear at most once.

The following additional rules apply:

* At most one of the methods can have the annotation `@OtherTokens`.
* At most one method can have the annotation `@EverythingAfter`. 
  `@OtherTokens` and `@EverythingAfter` cannot appear on the same method.
* Methods that have the `@OtherTokens` or `@EverythingAfter` annotation are called *special*. 
  All others are called *regular*.
* A special method must return `List<String>`.
* A regular method may have the `@LongName` or `@ShortName` annotation, or both.
* If a regular method has neither the `@LongName` nor `@ShortName` annotation,
  then by default the method name becomes the long name, and there is no short name.

This documentation will be extended over time. Meanwhile, check out the examples folder, and 
this [real-life example](https://github.com/h908714124/aws-glacier-multipart-upload/blob/master/src/main/java/ich/bins/ArchiveMPU.java).

### Example: `curl`

````java
@CommandLineArguments
abstract class Curl {

  @ShortName('X')
  @Description("Optional<String> for regular arguments")
  abstract Optional<String> method();

  @ShortName('H')
  @Description("List<String> for repeatable arguments")
  abstract List<String> headers();

  @ShortName('v')
  @Description("boolean for flags")
  abstract boolean verbose();

  @OtherTokens
  @Description({
      "@OtherTokens to capture everything else.",
      "In this case, everything that isn't '-v' or follows '-H' or '-X'"})
  abstract List<String> urls();
}
````

* `@CommandLineArguments` triggers the code generation. The generated code requires Java 8.
* Class `Curl_Parser` will be generated in the same package.

`Curl_Parser` has only one method, but there's also an `enum` to consider.

* The generated enum `Curl_Parser.Option` has constants `HEADERS`, `VERBOSE`, `METHOD` and `URLS`.
  These correspond to the constructor arguments, and have methods to generate usage text.
* The generated static method `Curl_Parser.parse(String[] args)` 
  takes the `args` argument from `public static void main(String[] args)`.
* `parse` returns an implementation of `Curl`.
* `parse` will throw `IllegalArgumentException` if it cannot make sense of the input.

Let's see how `Curl_Parser.parse(String[] args)` handles some input.
For example, if `args` is

* `{--method, --method}`, then `method()` will return the string `--method`. 
* `{--method=}`, then `method()` will return the empty string.
* `{--method}` or `{-X}`, then `Curl_Parser.parse()` will throw `IllegalArgumentException`
* `{-v, false}` then `verbose()` returns `true` and `urls()` returns the string `false`.
* `{}` (an empty array), then `method()` returns `null`, and `urls` returns an empty list.
* `{-Xда, -XНет}` leads to `IllegalArgumentException`.
* `{-v, -v}` (repeated flag) leads to `IllegalArgumentException` as well.

The next example shows how to use `@EverythingAfter`.
This can be used to take care of some syntactic corner cases that may arise if `@OtherTokens` is used.

### Example: `rm` constructor

````java
@CommandLineArguments
abstract class Rm {

  @ShortName('r')
  abstract boolean recursive();

  @ShortName('f')
  abstract boolean force();

  @OtherTokens
  abstract List<String> otherTokens();

  @EverythingAfter("--")
  @Description({
      "@EverythingAfter to create a last resort",
      "for problematic @OtherTokens.",
      "For example, when the file name is '-f'"})
  abstract List<String> filesToDelete();
}
````

If you're not familiar with `rm`'s `--` option, try `echo >>-f` and deleting the file it creates.

### The maven side

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

````xml
<dependency>
  <groupId>com.github.h908714124</groupId>
  <artifactId>jbock</artifactId>
  <version>2.0</version>
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
        <groupId>${project.groupId}</groupId>
        <artifactId>jbock</artifactId>
        <version>${project.version}</version>
      </dependency>
    </annotationProcessorPaths>

  </configuration>
</plugin>
````

### Java 9 config

The [examples project](https://github.com/h908714124/jbock/tree/master/examples) uses Java 9.
In order to use jbock on the module path, add the following to `module-info.java`:

````java
requires net.jbock;
````
