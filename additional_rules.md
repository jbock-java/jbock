## Additional rules

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
