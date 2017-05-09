# jbock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

jbock is a simple annotation processor that generates a [getopt_long](https://www.gnu.org/software/libc/manual/html_node/Getopt.html)-inspired
CLI parser. It can be used to define both short and long options.

Just like the default behaviour of `getopt_long`, its behaviour is not `POSIXLY_CORRECT`:
non-options do not stop option parsing, so options and non-options can be in any order.

### What sets it apart

* No reflection, purely static analysis.
* No runtime dependency. The generated `*_Parser.java` is self-contained.
* Convenient, flexible property binding via constructor.
* Deliberately simple: `String` and `boolean` only.
  No <em>converters</em>, no <em>default values</em>, no <em>required</em>.

### Features

* Short args can be written `-n1` or `-n 1` style.
* Long args can be written `--num=1` or `--num 1` style.
* A long flag is written `--zap`, a short flag `-z`.
* Grouping is possible, as in `tar -xzf d.tgz`.
* "Non-options", like in `rm foo.txt`: Use `@OtherTokens`
* "End of option scanning", like in `rm -- foo.txt`: Use `@EverythingAfter("--")`

### Basic usage

Annotate a constructor with `@CommandLineArguments`.
In this constructor, only three types of parameters are allowed:

* A `boolean` parameter declares a flag.
* A `List<String>` parameter declares a repeatable argument.
* A `String` parameter declares an argument that can appear at most once.

The following additional rules apply:

* At most one of the parameters can have the annotation `@OtherTokens`.
* At most one parameter can have the annotation `@EverythingAfter`. 
  `@OtherTokens` and `@EverythingAfter` cannot appear on the same parameter.
* Parameters that have the `@OtherTokens` or `@EverythingAfter` annotation are called "special". 
  All others are called "regular".
* Special parameters must be of type `List<String>`.
* Regular parameters can have the `@LongName` or `@ShortName` annotation, or both.
* If a regular parameter has neither `@LongName` nor `@ShortName`, 
  then by default the parameter name becomes the long name, and it there is no short name.

This documentation will be extended over time. Meanwhile, check out the examples folder, and 
this [real-life example](https://github.com/h908714124/aws-glacier-multipart-upload/blob/master/src/main/java/ich/bins/ArchiveMPU.java).

### Example: curl

````java
final class Curl {

  final List<String> headers;
  final List<String> urls;
  final String method;
  final boolean verbose;

  @CommandLineArguments
  Curl(@ShortName('H') @Description(
           "List<String> for arguments that can appear multiple times")
           List<String> headers,
       @ShortName('v') @Description(
           "boolean for value-less arguments, a.k.a. flags")
           boolean verbose,
       @ShortName('X') @LongName("method") @Description(
           "String for arguments that can appear at most once")
           String method,
       @OtherTokens @Description({
           "@OtherTokens to capture everything else.",
           "In this case, everything that isn't a verbose flag,",
           "a header or a HTTP method."})
           List<String> urls) {
    this.headers = headers;
    this.verbose = verbose;
    this.method = method == null ? "GET" : method;
    this.urls = urls;
  }
}
````

* `@CommandLineArguments` triggers the code generation. The generated code requires Java 8.
* Class `Curl_Parser` will be generated in the same package.

`Curl_Parser` has only one method, but there's also an `enum` to consider.

* The generated enum `Curl_Parser.Option` has constants `HEADERS`, `VERBOSE`, `METHOD` and `URLS`.
  These correspond to the constructor arguments, and have methods to <b>generate usage text</b>.
* The generated static method `Curl_Parser.parse(String[] args)` 
  takes the `args` argument from `public static void main(String[] args)`.
* `parse` returns another generated class, `Curl_Parser.Binder`.
* `parse` will throw `IllegalArgumentException` if it cannot make sense of the input.

The generated class `Curl_Parser.Binder` has two methods:

* `binder.bind()` invokes the constructor. It returns a `Curl` instance.
* `binder.otherTokens()` returns a `List<String>`. Unless `@OtherTokens` are already used in your constructor,
   should be inspected before invoking `bind()`, to inform the user about a possible input error.

Let's see how `Curl_Parser.parse(String[] args)` handles some good old input.
For example, if `args` is

* `{--method, --method}`, then `method` will be the string `--method`. 
* `{--method=}`, then `method` will be the empty string.
* `{--method}` or `{-X}`, then `parse` will throw `IllegalArgumentException`
* `{-v, false}` then `verbose` is `true` and `urls` contains the string `false`.
* `{}` (an empty array), then `method` is `null`, and `urls` is an empty list.
* `{-Xда, -XНет}` is `IllegalArgumentException`.
* `{-v, -v}` (repeated flag) is `IllegalArgumentException` as well.

The next example shows how to use `@EverythingAfter`.
This can be used to take care of some syntactic corner cases that may arise if `@OtherTokens` is used.

### Example: rm

````java
final class Rm {

  final boolean recursive;
  final boolean force;
  final List<String> filesToDelete;

  @CommandLineArguments
  Rm(@ShortName('r') boolean recursive,
     @ShortName('f') boolean force,
     @OtherTokens List<String> fileNames,
     @EverythingAfter("--") @Description({
         "@EverythingAfter can be used as an 'escape mechanism'",
         "for unnamed arguments, a.k.a. @OtherTokens.",
         "For example, to specify a file named '-f'"})
         List<String> moreFileNames) {
    this.recursive = recursive;
    this.force = force;
    this.filesToDelete = Stream.of(fileNames, moreFileNames)
        .map(List::stream)
        .flatMap(Function.identity())
        .collect(Collectors.toList());
  }
}
````

If you're not familiar with `rm`'s `--` option, try `echo >>-f` and deleting the file it creates.

### The boring side: Maven technicalities

````xml
<dependency>
  <groupId>com.github.h908714124</groupId>
  <artifactId>jbock</artifactId>
  <version>1.7</version>
  <scope>provided</scope>
</dependency>
````

The `jbock` artifact is not needed at runtime.

There's also a separate `jbock-annotations` jar
if you want to go fancy and use gradle's `apt` plugin.
