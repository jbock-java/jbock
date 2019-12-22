[![core](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg?style=plastic&subject=jbock)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)
[![annotations](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations/badge.svg?color=red&style=plastic&subject=jbock-annotations)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock-annotations)

Params and options are defined as abstract methods:

````java
@Command
abstract class MyArguments {

  /**
   * A "param" is a positional parameter.
   */
  @Param(1)
  abstract Path path();

  /** 
   * This javadoc will show up when "--help" is passed.
   * Alternatively you can define the help text in a resource bundle.
   */
  @Option(value = "verbosity", mnemonic = 'v')
  abstract OptionalInt verbosity();
}
````

Note, the number `1` is arbitrary as long as there is only one `@Param`.

If jbock is properly configured as an
[annotation processor](https://stackoverflow.com/questions/2146104/what-is-annotation-processing-in-java),
then the presence of this annotated class
will trigger a round of code generation at compile time.

This will generate a class called
[MyArguments_Parser,](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/hello/MyArguments_Parser.java)
which can be used as follows:

````java
String[] args = { "-v2", "file.txt" }; // from command line
MyArguments my = MyArguments_Parser.create().parseOrExit(args);

// make sure this works as expected...
assertEquals(OptionalInt.of(2), my.verbosity());
assertEquals(Paths.get("file.txt"), my.path());
````

Please note that `path` is a *required* param,
while `verbosity` is an *optional* option.
This is determined from the methods' return type,
using the following "matching" rules (upper left corner applies for both parameters):

### Parameter type matching

<table style="border-collapse: collapse">
<tr>
<td></td>
<td><b>No mapper defined</b></td>
<td><b>Mapper defined</b></td>
</tr>
<tr>
<td valign="top"><b>No<br/>collector<br/>defined</b></td>
<td>
<table><!-- No mapper, no collector-->
<tr><td><code>boolean | Boolean</code>        </td><td><i>flag*</i></td></tr>
<tr><td><code>X</code>                        </td><td><i>required</i></td></tr>
<tr><td><code>Optional&lt;X&gt;</code>        </td><td><i>optional</i></td></tr>
<tr><td><code>OptionalInt</code> etc.         </td><td><i>optional</i></td></tr>
<tr><td><code>List&lt;X&gt;</code>            </td><td><i>repeatable</i></td></tr>
</table>
</td>
<td>
<table><!-- Mapper, no collector-->
<tr><td><code>R</code>                        </td><td><i>required</i></td></tr>
<tr><td><code>Optional&lt;R&gt;</code>        </td><td><i>optional</i></td></tr>
<tr><td><code>OptionalInt</code> (if <code>R == Integer</code>)         </td><td><i>optional</i></td></tr>
<tr><td><code>OptionalLong</code> (if <code>R == Long</code>)         </td><td><i>optional</i></td></tr>
<tr><td><code>OptionalDouble</code> (if <code>R == Double</code>)         </td><td><i>optional</i></td></tr>
<tr><td><code>List&lt;R&gt;</code>            </td><td><i>repeatable</i></td></tr>
</table>
</td>
</tr>
<tr>
<td><b>Collector<br/>defined</b></td>
<td colspan="2"><i>repeatable</i></td>
</tr>
</table>

<i>*</i> : <i>does not apply to positional parameters</i>

where `X` is one of the
"[auto types](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/hello/JbockAutoTypes.java)",
and `R` is the return type of the mapper.

* [Detailed documentation](https://github.com/h908714124/jbock/blob/master/SPAGHETTI.md)
* [jbock-maven-example](https://github.com/h908714124/jbock-maven-example)
* [jbock-gradle-example](https://github.com/h908714124/jbock-gradle-example)
