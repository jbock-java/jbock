# jbock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

`jbock 2.3` is an annotation processor that generates a
[getopt_long](https://www.gnu.org/software/libc/manual/html_node/Getopt.html)-like
CLI parser, with an extra focus on positional parameters.

Its primary goals are:
 
* To generate concise parser code from a command line interface that's declared via annotations.
* To give the end user clear feedback via exceptions, if the input is invalid.
* To print usage text that looks similar to a GNU man page.

### User feedback is important.

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

Being invoked without any arguments is something we should be expecting.
Instead of showing a stacktrace, we could use this opportunity to inform the user
about our command line options. Let's see how to do this with `jbock 2.3`.

We start by defining an abstract class `Args`,
which represents the two mandatory arguments `src` and `dest`:

````java
@CommandLineArguments
abstract static class Args {
  @Positional abstract String src();
  @Positional abstract String dest();
}
````

Here, the source order of the method declarations `src()` and `dest()` matters.
At compile time, the jbock processor will pick up the
`@CommandLineArguments` annotation, and generate a class called 
`CopyFile_Args_Parser` in the same package. Let's change
our main method to use that instead.
This is the complete code:

````java
public class CopyFile {

  @CommandLineArguments
  abstract static class Args {
    @Positional abstract String src();
    @Positional abstract String dest();
  }

  public static void main(String[] args) {
    CopyFile_Args_Parser.parse(args, System.out)
        .ifPresentOrElse(
            System.out::println,
            () -> System.exit(1));
  }
}
````
This is what the updated program prints, when invoked without
any arguments:

<pre><code>Usage: CopyFile SRC DEST
Missing parameter: SRC
Try '--help' for more information.
</code></pre>

This looks a lot better than the stacktrace already.
Going forward, we should also add some metadata:

````java
@CommandLineArguments(
    programName = "cp",
    missionStatement = "copy files and directories",
    overview = "There are no options yet.")
abstract static class Args {
  @Positional abstract String src();
  @Positional abstract String dest();
}
````

When the program is now invoked with the `--help` parameter,
it will print something resembling a [man page](https://linux.die.net/man/1/cp):

<pre><code>NAME
       cp - copy files and directories

SYNOPSIS
       cp SRC DEST

DESCRIPTION
       There are no options yet.
</code></pre>
