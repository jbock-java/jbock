# jbock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

`jbock` is an annotation-driven command line parser, similar to [jcommander](http://jcommander.org/), but doesn't use reflection.
Instead, it generates custom source code through a mechanism called Java annotation processing.

## Introduction

Let's write a program that copies a file. This is what it might look like in Java:

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
a summmary of our command line options. Let's see how to do this with jbock.

We start by defining an abstract class `Args`,
which represents the two mandatory arguments `source` and `dest`.

````java
import net.jbock.*;
import java.nio.file.Path;

@CommandLineArguments
abstract class Args {
  @Positional abstract Path source();
  @Positional abstract Path dest();
}
````

In  this case, the order of the method declarations `source()` and `dest()` matters,
because they represent positional arguments.
`source()` is declared first, so it represents the first argument.

At compile time, the jbock processor will pick up the
`@CommandLineArguments` annotation, and generate a class 
`Args_Parser extends Args` in the same package. Let's change
our main method to use that.

This is the updated `CopyFile` class:

````java
public class CopyFile {

  public static void main(String[] input) {
    Args args = Args_Parser.create().parseOrExit(input);
    Files.copy(args.source(), args.dest());
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
abstract class Args {
  @Positional abstract Path source();
  @Positional abstract Path dest();
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
@CommandLineArguments
abstract class Args {
  @Positional abstract Path source();
  @Positional abstract Path dest();
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
@CommandLineArguments
abstract class Args {
  @Positional abstract Path source();
  @Positional abstract Path dest();
  @ShortName('r') abstract boolean recursive();
}
````

Now let's add another flag called `--backup` or `-b`, 
and an optional parameter called `--suffix` or `-s`:

````java
@CommandLineArguments
abstract class Args {
  @Positional abstract Path source();
  @Positional abstract Path dest();
  @ShortName('r') abstract boolean recursive();
  @ShortName('b') @LongName("") abstract boolean backup();
  @ShortName('S') abstract Optional<String> suffix();
}
````

For each named (i.e. non-positional) parameter, the method name defines the long name. 
This can be overridden with the `@LongName` annotation,
or disabled by passing the empty string to the `@LongName` annotation.

After adding some description text, the Args class looks like this:

````java
@CommandLineArguments(
    programName = "cp",
    missionStatement = "copy files and directories",
    overview = "Copy SOURCE to DEST")
abstract class Args {
  @Positional abstract Path source();
  @Positional abstract Path dest();

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
folder has demos and tests for most parser features.

Some projects that use jbock:

* [aws-glacier-multipart-upload](https://github.com/h908714124/aws-glacier-multipart-upload)
* [wordlist-extendible](https://github.com/WordListChallenge/wordlist-extendible)
* [jbock-gradle-example](https://github.com/h908714124/jbock-gradle-example)

