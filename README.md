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
  final String url;
  final boolean verbose;

  Curl (@ShortName('H') List<String> headers,
        @LongName("url") String url,
        @ShortName('v') boolean verbose) {
    this.headers = headers;
    this.url = Objects.requireNonNull(url, "url is required");
    this.verbose = verbose;
  }
}
````

### Notes

* Class `CurlParser` will be generated in the same package, 
  with a static method `Binder CurlParser.parse(String[])`.
* `CurlParser.Option` is an `enum` of the constructor arguments.
* `CurlParser#parse` will throw `IllegalArgumentException` if the input looks wrong.
* `Binder#bind()` returns the `Curl` instance
* `Binder#trash()` contains unknown / ignored tokens.
  It is probably a good idea to check and even abort if this isn't empty.
  An advanced use may read the `url` parameter from the trash.
* `Binder#arguments()` returns the parse result for closer inspection, before invoking `bind()`.
* Arguments of type `boolean` are called <em>flags</em>.
* Only arguments of type `String`, `List<String>` and `boolean` may be declared in the constructor.
  One possible place for conversions such as `Integer.parseInt` would be inside your annotated constructor.
* `boolean` arguments define "flags". These do not take arguments! If `-v` is a flag, 
  then `"-v false"` means that `-v` is <em>true</em>, and the unknown token `false` is ignored. 
  Check out the `Binder#trash()` method in the generated code.
* Each argument may have both a short and long name.
* If neither `@ShortName` nor `@LongName` is specified, 
  then flags get a default short name and non-flags get a default long name.
  The argument name is used for these.
* `String` arguments will be `null`, unless present. 
  There's no built-in concept of required options.
