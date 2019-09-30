## jbock [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

jbock is a simple and convenient ~~application server~~ *command line parser.*
The command line options are defined as abstract methods:

````java
@CommandLineArguments
abstract class MyArguments {

  @PositionalParameter
  abstract Path path();

  @Parameter(shortName = 'v')
  abstract OptionalInt verbosity();
}
````

The mere presence of this annotated class
triggers the code generation at compile time.
A derived class `MyArguments_Parser`
will be generated, which
can be used in a `main` method as follows:

````java
String[] argv = { "-v2", "file.txt" }; // for example
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

// make sure this works as expected...
assertEquals(OptionalInt.of(2), args.verbosity());
assertEquals(Paths.get("file.txt"), args.path());
````

In the example above, note that `path` is a required parameter,
and `verbosity` is optional.
This is determined by the parameter type:

<table style="border-collapse: collapse">
<tr>
<td></td>
<td><b>No mapper</b></td>
<td><b>Mapper defined</b></td>
</tr>
<tr>
<td valign="top"><b>No<br/>collector</b></td>
<td>
<table style="border-collapse: collapse; border: 1px solid black"><!-- No mapper, no collector-->
<tr><td><code>boolean | Boolean</code>  </td><td><i>flag*</i></td></tr>
<tr><td><code>Optional&lt;X&gt;</code>        </td><td><i>optional</i></td></tr>
<tr><td><code>List&lt;X&gt;</code>            </td><td><i>repeatable</i></td></tr>
<tr><td><code>X</code>                  </td><td><i>required</i></td></tr>
</table>
</td>
<td>
<table style="border-collapse: collapse; border: 1px solid black"><!-- Mapper, no collector-->
<tr><td><code>Optional&lt;R&gt;</code>   </td><td><i>optional</i></td></tr>
<tr><td><code>List&lt;R&gt;</code>       </td><td><i>repeatable</i></td></tr>
<tr><td><code>R</code>             </td><td><i>required</i></td></tr>
</table>
</td>
</tr>
<tr>
<td><b>Collector<br/>defined</b></td>
<td colspan="2" style="text-align: center"><i>repeatable</i></td>
</tr>
</table>

<i>*</i> : <i>does not apply to positional parameters</i>

where `X` is the parameter type (must be one of the
"[auto types](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/hello/JbockAutoTypes.java)"),
and `R` is the return type of the mapper.
`OptionalInt` and friends can be used in place of `Optional<Integer>` etc.

* [Detailed documentation](https://github.com/h908714124/jbock/blob/master/READ_MORE.md)
* [Gradle example](https://github.com/h908714124/jbock-map-example)
* [Maven example](https://github.com/h908714124/jbock-maven-example)

