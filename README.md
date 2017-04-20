# jBock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

Annotation processor that allows binding of command line options to instance fields.

The goal is to easily define GNU-style command line interface, via annotations.

This documentation will be extended over time. Meanwhile, check out the examples folder, and 
this [real-life example](https://github.com/h908714124/aws-glacier-multipart-upload).

### Example

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

### Notes

* Long options must always be passed `--key=VALUE` style.
* Short options may be passed either `-k value` or `-kvalue` style.
* Only arguments of type `String`, `List<String>` and `boolean` may be declared in the constructor.
* Repeating keys are possible, if the corresponding constructor argument is of type `List<String>`.
* `boolean` arguments are called "flags". They do <em>not</em> take arguments. In the example above, 
  `"-v false"` means that `verbose` is <em>true</em>, and `"false"` goes in `urls`.
* Absent `String` arguments will be passed as `null` to the constructor.
* There's no built-in concept of required options.
  Consider performing null-checks in the constructor.
* Class `CurlParser` will be generated in the same package, 
  with a static method `CurlParser.Binder CurlParser#parse(String[])`.
* `CurlParse.Binder#bind()` invokes the constructor.
* `CurlParser.Option` is a generated `enum` of the constructor arguments.
* `CurlParser#parse` will throw `IllegalArgumentException` if the input is invalid, 
  like `-XGET -XPOST`.
* There's no built-in concept of converters. 
  One possible place for conversions, such as `Integer.parseInt`, would be inside the constructor.
* Each argument may have both a short and long name.
* If neither `@ShortName` nor `@LongName` is specified,
  then the argument name becomes the default long name, and no short name is defined.
