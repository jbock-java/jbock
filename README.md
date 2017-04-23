# jBock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

jBock is a simple annotation processor that generates `GNU` and `posix` compliant CLI parsers.

### What sets it apart

* No reflection, purely static analysis.
* No runtime dependency. The generated code is self-contained.
* Convenient, flexible property binding via constructor
* Stupidly simple: `String` and `boolean` only. 
  No <em>converters</em>, no <em>default values</em>, no <em>required</em>.

### Features

* Short form can be written attached `-n1` or detached `-n 1` style
* Long form must always be written `--key=VALUE` style (except flags, of course)
* Flags: Declare a `boolean` parameter
* Repeating keys: Declare a `List<String>`
* Unnamed arguments, like in `rm foo.txt`: Use `@OtherTokens`
* `rm -- foo.txt` style escaping: Use `@EverythingAfter("--")`

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
           "In this case, everything that isn't '-v' or follows '-H' or '-X'"})
           List<String> urls) {
    this.headers = headers;
    this.verbose = verbose;
    this.method = method == null ? "GET" : method;
    this.urls = urls;
  }
}
````

* `@CommandLineArguments` triggers the code generation. The generated code requires Java 8.
* Class `CurlParser` will be generated in the same package

`CurlParser` has only one method, but there's also an `enum` to consider.

* `enum CurlParser.Option` has constants `HEADERS`, `VERBOSE`, `METHOD` and `URLS`.
  These have useful methods, like for generating printable documentation.
* The static method `parse(String[] args)` is ready for invokin' from ye olde `public static void main`.
* `parse` returns a `CurlParser.Binder`.
* `parse` will throw `IllegalArgumentException` if it cannot make sense of the input.

The `CurlParser.Binder binder` has several methods, but you'll probably need only one:

* `binder.bind()` invokes the constructor. It returns a `Curl` instance.

Further musings&#8230;

* A lonely `--key=` token binds the empty string to `key`.
* At most one argument may have the `@OtherTokens` annotation. Otherwise, compile error is what you get.
* Flags do <em>not</em> take arguments. In the example above,
  `-v false` would mean that `verbose` is <em>true</em>, and that `urls` contains the string <em>false</em>.
* If `--method=SOMETHING` or `-XSOMETHING` token is absent, `method` will be `null`.
* If both `-Xда` and `-XНет` tokens are present, `parse` will throw `IllegalArgumentException`
* If neither `@ShortName` nor `@LongName` is specified, then there is no short name,
  and the argument name becomes the long name by default.

The next example shows how to use `@EverythingAfter`.
This can be used to take care of some syntactic corner cases that arise with `@OtherTokens`

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

* If you're not familiar with `rm`'s `--` option, try `echo >>-f` and deleting the file it creates.
* Like `@OtherTokens`, at most one constructor argument can be `@EverythingAfter`
* `@OtherTokens` and `@EverythingAfter` cannot appear on the same argument

### The boring side: Maven technicalities

````xml
<dependency>
  <groupId>com.github.h908714124</groupId>
  <artifactId>jbock</artifactId>
  <version>1.1</version>
  <scope>provided</scope>
</dependency>
````

The `jbock` artifact is not needed at runtime.

There's also a separate `jbock-annotations` jar
if you want to go fancy and use gradle's `apt` plugin.
