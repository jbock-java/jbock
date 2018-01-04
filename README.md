# jbock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

`jbock 2.3` is an annotation processor that generates a
[getopt_long](https://www.gnu.org/software/libc/manual/html_node/Getopt.html)-like
CLI parser, with an extra focus on positional parameters.

Its primary goals are:
 
* To generate concise parser code from a command line interface that's declared via annotations.
* To give the end user clear feedback if the input is invalid.
* To print usage text that looks similar to a GNU man page, when the `--help` parameter is passed.

### User feedback is important.

To clarify these goals, we're going to write a simple command line utility that copies a file.

````java
public class CopyFile {

  public static void main(String[] args) throws IOException {
    String source = args[0];
    String dest = args[1];
    Files.copy(Paths.get(source), Paths.get(dest));
    System.out.printf("Done copying %s to %s%n", source, dest);
  }
}
````

This is what the program prints, when invoked without parameters:

<pre><code>Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: 0
    at cli.tools.CopyFile.main(CopyFile.java:10)
</code></pre>

Being invoked without any arguments is something we should be expecting.
Instead of a stacktrace, we could use the opportunity to show the user
a summmary of our command line options. Let's see how to do this with `jbock 2.3`.

We start by defining an abstract class `Args`,
which represents the two mandatory arguments `source` and `dest`.

````java
@CommandLineArguments
abstract static class Args {
  @Positional abstract String source();
  @Positional abstract String dest();
}
````

For brevity, we will sometimes omit the `static` keyword and the
`@CommandLineArguments` annotation when we 
talk about this class.

Because they represent positional arguments,
the order of the method declarations `source()` and `dest()` matters.
`source()` is declared first, because it represents the first argument.

At compile time, the jbock processor will pick up the
`@CommandLineArguments` annotation, and generate a class called 
`CopyFile_Args_Parser` in the same package. Let's change
our main method to use that.
This is the updated `CopyFile` class:

````java
public class CopyFile {

  @CommandLineArguments
  abstract static class Args {
    @Positional abstract String source();
    @Positional abstract String dest();
  }

  public static void main(String[] args) {
    CopyFile_Args_Parser.parse(args, System.out)
        .ifPresentOrElse(
            CopyFile::run,
            () -> System.exit(1));
  }

  private static void run(Args args) {
    try {
      Files.copy(Paths.get(args.source()), Paths.get(args.dest()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
````
After this change, the program prints
the following when invoked without any arguments:

<pre><code>Usage: CopyFile SRC DEST
Missing parameter: SRC
Try '--help' for more information.
</code></pre>

This looks already a lot better than the stacktrace.
Next, we add some metadata:

````java
@CommandLineArguments(
    programName = "cp",
    missionStatement = "copy files and directories",
    overview = "There are no options yet.")
abstract static class Args {
  @Positional abstract String source();
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

To make things more interesting, we're going to add some options.
We start by adding the recursive flag.
With jbock, an option is declared as an abstract method.
A <em>flag</em> is a special kind of option,
one that doesn't take a value.
It is declared as a method that returns `boolean`.

````java
abstract class Args {
  @Positional abstract String source();
  @Positional abstract String dest();
  abstract boolean recursive();
}
````

The `recursive` argument is not positional:
Its declaring method doesn't have a `@Positional` annotation.
Because of this, it doesn't matter whether it 
is declared before or after 
the `source()` and `dest()` methods, or between them.

Since `--recursive` is such a common flag,
we also add its usual short form:

````java
abstract class Args {
  @Positional abstract String source();
  @Positional abstract String dest();
  @ShortName('r') abstract boolean recursive();
}
````

Now let's also add the backup and suffix options:

````java
abstract class Args {
  @Positional abstract String source();
  @Positional abstract String dest();
  @ShortName('r') abstract boolean recursive();
  @ShortName('b') @LongName("") abstract boolean backup();
  @ShortName('S') abstract Optional<String> suffix();
}
````

For each non-positional option, the method name is the long name
by default. This default can be overridden with the `@LongName` annotation,
or disabled by passing the empty string.

After these changes, the complete code looks as follows:

(The `run` method would become too long,
so it has been replaced with a simple
print statement.
Also, we've added some `@Description` annotations.)

````java
public class CopyFile {

  @CommandLineArguments(
      programName = "cp",
      missionStatement = "copy files and directories",
      overview = "Copy SOURCE to DEST")
  abstract static class Args {
    @Positional abstract String source();
    @Positional abstract String dest();

    @ShortName('r')
    @Description("copy directories recursively")
    abstract boolean recursive();

    @ShortName('b') @LongName("")
    @Description("make a backup of each existing destination file")
    abstract boolean backup();

    @ShortName('S')
    @Description("override the usual backup suffix")
    abstract Optional<String> suffix();
  }

  public static void main(String[] args) {
    CopyFile_Args_Parser.parse(args, System.out)
        .ifPresentOrElse(
            CopyFile::run,
            () -> System.exit(1));
  }

  private static void run(Args args) {
    System.out.println("Copying files: " + args);
  }
}
````

The following is printed when the `--help`
parameter is passed:

<pre><code>NAME
       cp - copy files and directories

SYNOPSIS
       cp [OPTION]... SOURCE DEST

DESCRIPTION
       Copy SOURCE to DEST

       -r, --recursive
              copy directories recursively

       -b
              make a backup of each existing destination file

       -S, --suffix VALUE
              override the usual backup suffix
</code></pre>

The full source code of the <em>CopyFile</em>
project can be found 
[here](https://github.com/h908714124/CopyFile).

While there is no formal specification of jbock yet,
the
[examples](https://github.com/h908714124/jbock/tree/master/examples)
contain unit tests that demonstrate
the most important features.
