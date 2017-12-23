# jbock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

jbock is an annotation processor that generates a 
[getopt_long](https://www.gnu.org/software/libc/manual/html_node/Getopt.html)-like
CLI parser, with an extra focus on positional parameters.

Its primary goals are:
 
* To generate concise parser code from a command line interface that's declared via annotations.
* To give the end user clear feedback via exceptions, if the input is invalid.
* To print usage text that looks similar to a GNU man page.

### User feedback is hard!

To clarify these goals, we're going to write a simple command line utility that copies a file.

````java
public class CopyFile {

  public static void main(String[] args) throws IOException {
    String src = args[0];
    String dest = args[1];
    Files.copy(Paths.get(src), Paths.get(dest));
    System.out.printf("Done copying %s to %s%n", src, dest);
  }
}
````

This is what the program prints, when invoked without parameters:

<pre><code>Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: 0
    at cli.tools.CopyFile.main(CopyFile.java:10)
</code></pre>

### That's cool, but we can do better

Printing a stacktrace usually means that an unexpected condition has caused our program to crash.
But there's no reason to crash here.

We will now use jbock to print a better error message for the user.
We start by defining an abstract class `Args`,
which represents the two mandatory arguments to our program:

````java
@CommandLineArguments
abstract class Args {
  @Positional abstract String src();
  @Positional abstract String dest();
}
````

Here, the source order of the method declarations `src()` and `dest()` matters.

<pre><code>UNDER CONSTRUCTION</code></pre>
