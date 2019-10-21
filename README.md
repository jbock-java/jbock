jbock
[![core](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)
[![annotations](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations/badge.svg?color=red&style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations)
is a convenient *command line parser* that generates Java source code,
rather than using reflection.
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

If your build tool is set up correctly, the presence of this annotated class
will trigger code generation at compile time.
Specifically, a class named `MyArguments_Parser`
will be generated. The result, which can be seen
[here,](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/hello/MyArguments_Parser.java)
can be used in a *main* method:

````java
String[] argv = { "-v2", "file.txt" }; // for example
MyArguments args = MyArguments_Parser.create().parseOrExit(argv);

// make sure this works as expected...
assertEquals(OptionalInt.of(2), args.verbosity());
assertEquals(Paths.get("file.txt"), args.path());
````

In the example above, note that `path` is a required parameter,
and `verbosity` is optional.
This is determined by the parameter type,
using the following rule (upper left corner applies):

<table style="border-collapse: collapse">
<tr>
<td></td>
<td><b>No mapper</b></td>
<td><b>Mapper defined</b></td>
</tr>
<tr>
<td valign="top"><b>No<br/>collector<br/>defined</b></td>
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

where `X` is one of the
"[auto types](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/hello/JbockAutoTypes.java)",
and `R` is the return type of the mapper.
`OptionalInt` and friends can be used in place of `Optional<Integer>` etc.

* [Detailed documentation](https://github.com/h908714124/jbock/blob/master/SPAGHETTI.md)
* [Gradle example](https://github.com/h908714124/jbock-map-example)
* [Maven example](https://github.com/h908714124/jbock-maven-example)

