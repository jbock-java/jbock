# jbock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

`jbock 2.3` is an annotation processor that generates a
[getopt_long](https://www.gnu.org/software/libc/manual/html_node/Getopt.html)-like
command line parser.

Its goals are:
 
* To generate convenient, readable parser code from Java annotations.
* To give the user of the command line interface a good error message, if the input is invalid.
* To give the developer of the command line interface a good error message, if there is a configuration error.
* To print usage text that looks similar to a GNU man page, when the `--help` parameter is passed.

## Introduction

For starters, let's write a program that copies a file.

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

Being invoked without any parameters is something we should be expecting.
Instead of a stacktrace, we could use the opportunity to show
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

In  this case, the order of the method declarations `source()` and `dest()` matters,
because they represent positional arguments.
`source()` is declared first, so it represents the first argument.

At compile time, the jbock processor will pick up the
`@CommandLineArguments` annotation, and generate a class 
`CopyFile_Args_Parser extends CopyFile.Args` in the same package. Let's change
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
    Optional<Args> opt = CopyFile_Args_Parser.parse(args, System.err);
    if (!opt.isPresent()) {
      System.exit(1);
    }
    opt.ifPresent(CopyFile::run);
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
After the change, the program prints
the following when invoked without any arguments:

<pre><code>Usage: CopyFile SRC DEST
Missing parameter: SRC
Try 'CopyFile --help' for more information.
</code></pre>

This looks a lot better than the stacktrace already.

## Improving the command line interface

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

In jbock, an option is declared as an abstract method that doesn't take any parameters.

A <em>flag</em> is declared as a method that returns `boolean`.
This method will return true if the flag is present on the command line.

````java
abstract class Args {
  @Positional abstract String source();
  @Positional abstract String dest();
  abstract boolean recursive();
}
````

The `recursive` argument is not positional, because
its declaring method doesn't have a `@Positional` annotation.
It makes no difference whether it 
is declared before or after
the `source()` and `dest()` methods, or between them.

Since `--recursive` is such a common flag,
we also allow the shortcut `-r`:

````java
abstract class Args {
  @Positional abstract String source();
  @Positional abstract String dest();
  @ShortName('r') abstract boolean recursive();
}
````

Now let's add another flag called `--backup` or `-b`, 
and an optional parameter called `--suffix` or `-s`:

````java
abstract class Args {
  @Positional abstract String source();
  @Positional abstract String dest();
  @ShortName('r') abstract boolean recursive();
  @ShortName('b') @LongName("") abstract boolean backup();
  @ShortName('S') abstract Optional<String> suffix();
}
````

For each named (i.e. non-positional) parameter, the method name defines the long name. 
This can be overridden with the `@LongName` annotation,
or disabled by passing the empty string to the `@LongName` annotation.

After adding some description text, the complete code looks as follows:

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
    Optional<Args> opt = CopyFile_Args_Parser.parse(args, System.err);
    if (!opt.isPresent()) {
      System.exit(1);
    }
    opt.ifPresent(CopyFile::run);
  }

  private static void run(Args args) {
    System.out.println("Copying files: " + args);
  }
}
````

When the user passes the `--help`
parameter on the command line, this program now prints the following:

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

The
[examples](https://github.com/h908714124/jbock/tree/master/examples)
contain unit tests for many parser features.

And here's another example: [wordlist-extendible](https://github.com/WordListChallenge/wordlist-extendible)
