# jBock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

Annotation processor that allows binding of command line options to instance fields. It also generates an `enum` of the options, along with a printable description.

The goal is to easily define GNU-style command line interface, via annotations.

This documentation will be extended over time. Meanwhile, check out the examples folder, and 
this [real-life example](https://github.com/h908714124/aws-glacier-multipart-upload).

### Example: curl

````java
final class Curl {

  final List<String> headers;
  final List<String> urls;
  final String method;
  final boolean verbose;

  @CommandLineArguments
  Curl(@ShortName('H') @Description("List<String> for arguments that appear multiple times")
           List<String> headers,
       @ShortName('v') @Description("boolean for flags")
           boolean verbose,
       @ShortName('X') @Description("String for regular arguments")
           String method,
       @OtherTokens @Description("Everything that isn't '-v' or follows '-H' or '-X'")
           List<String> urls) {
    this.headers = headers;
    this.verbose = verbose;
    this.method = method == null ? "GET" : method;
    this.urls = urls;
  }
}
````

* `@CommandLineArguments` triggers the code generation.
* Class `CurlParser` will be generated in the same package,
  with a static method `CurlParser#parse(String[])` which returns a `CurlParser.Binder`.
* Long options must always be passed `--key=VALUE` style.
  `--key` is invalid and results in `IllegalArgumentException`.
  `--key=` binds an empty string.
* Short options may be passed either `-k value` or `-kvalue` style.
* Only `String`, `List<String>` and `boolean` arguments are allowed.
* At most one argument may have the `@OtherTokens` annotation.
  Every command line token that didn't bind to another argument, will be included in this list.
* Repeating keys are possible, if the corresponding constructor argument is of type `List<String>`.
* `boolean` arguments are called "flags". They do <em>not</em> take arguments. In the example above,
  `-v false` would mean that `verbose` is <em>true</em>, and that `urls` contains the string <em>false</em>.
* An absent `String` argument will be passed as `null`.
* There's no built-in concept of required options.
  Consider performing null-checks in the constructor.
* `CurlParser.Binder#bind()` invokes the constructor.
* `CurlParser.Option` is a generated `enum` of the constructor arguments.
* `CurlParser#parse` will throw `IllegalArgumentException` if the input is invalid,
  like `-XGET -XPOST`.
* There's no built-in concept of converters.
  One possible place for conversions, such as `Integer.parseInt`, would be inside the constructor.
* Each argument, except `@OtherTokens`, may have both a short and long name.
* If neither `@ShortName` nor `@LongName` is specified,
  then the argument name becomes the long name by default.

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
         "Last resort for problematic arguments",
         "For example, when file name is '-f'"})
         List<String> escapedFileNames) {
    this.recursive = recursive;
    this.force = force;
    this.filesToDelete = Stream.of(fileNames, escapedFileNames)
        .map(List::stream)
        .flatMap(Function.identity())
        .collect(Collectors.toList());
  }
}
````

* If you're not familiar with `rm`'s `--`: try `echo >>-f` and delete the file it creates.
* Like `@OtherTokens`, at most one argument can be `@EverythingAfter`
* `@OtherTokens` and `@EverythingAfter` cannot appear on the same argument
