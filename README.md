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
Instead of a stacktrace, we could use the opportunity to print
a summary of our command line options. Let's see how to do this with jbock.

We start by defining an abstract class `Args`,
which represents the two mandatory arguments `source` and `dest`.

````java
import net.jbock.PositionalParameter;
import net.jbock.CommandLineArguments;

import java.nio.file.Path;

@CommandLineArguments
abstract class Args {

  @PositionalParameter
  abstract Path source();

  @PositionalParameter
  abstract Path dest();
}
````

The methods that carry the `Parameter` or `PositionalParameter` annotation must be abstract.
They are called the <em>parameter methods</em>. 

In  this case, the order of the parameter methods `source()` and `dest()` matters,
because they represent positional arguments.
`source()` is declared first, so it represents the first argument.

At compile time, the jbock processor will pick up the
`@CommandLineArguments` annotation, and generate a class
`Args_Parser` that can be used to obtain an instance of `Args`.

Before we move on, we should mention two important guarantees that jbock gives you about its implementation. 

#### The non-null pledge

> None of the parameter methods will ever throw an exception 
or return `null`, regardless of the input to `parse` or `parseOrExit`.

This means that jbock will either give you either a fully initialized 
instance of your args class, or no instance at all. 
This is ensured by the generated constructor, 
which performs null checks for each non-primitive argument.

This also implies that any parameter method that doesn't return 
either `List` or `Optional` must represent a <em>required</em> argument,
which means that failure to pass it on the command line will cause parsing to fail.
Otherwise it would hardly be possible to fulfill this promise.

#### The no-exception pledge

> `parse` or `parseOrExit` will never throw an exception or return `null`.

To be fair though, it should be mentioned that `parseOrExit` <em>will</em>
shutdown the JVM if the input is invalid.

Now let's move on with our copy tool. A typical jbock invocation looks like this:

````java
public class CopyFile {

  public static void main(String[] input) {
    Args args = Args_Parser.create().parseOrExit(input);
    Files.copy(args.source(), args.dest());
  }
}
````

The `parseOrExit` method is very convenient.
If the input is not valid, it will print a helpful error message and shutdown the JVM,
which is often the most helpful behaviour.
If you want more control, you can use the `parse` method instead.

When invoked without any arguments, 
the program now prints the following message:

<pre><code>Usage: CopyFile SOURCE DEST
Missing parameter: SOURCE
Try 'CopyFile --help' for more information.
</code></pre>

This looks a lot better than the stacktrace already.

#### Adding metadata

Next, we add some metadata:

````java
/**
 * There are no options yet.
 */
@CommandLineArguments(
    programName = "cp",
    missionStatement = "copy files and directories")
abstract class Args {
  //[...]
}
````

When the program is invoked with the `--help` parameter,
it will use the attributes and also the javadoc, 
to print something resembling a [man page](https://linux.die.net/man/1/cp):

<pre><code>NAME
       cp - copy files and directories

SYNOPSIS
       cp &lt;SOURCE&gt; &lt;DEST&gt;

DESCRIPTION
       There are no options yet.
</code></pre>

#### Adding options

To make things more interesting, we're going to add some
non-positional parameters, a.k.a. options.
We start by adding the recursive flag.

A <em>flag</em> is an option that doesn't take an argument.
A method that returns `boolean` declares the flag.

This parameter method will return `true` if the `--recursive` option
is present on the command line, and `false` otherwise:

````java
@CommandLineArguments
abstract class Args {
  //[...]
  @Parameter
  abstract boolean recursive();
}
````

It makes no difference whether this new method
is declared before or after
the `source()` and `dest()` methods, or between them.

Since `--recursive` is such a common flag,
we also allow the usual shortcut `-r`:

````java
@CommandLineArguments
abstract class Args {
  //[...]
  @Parameter(shortName = 'r')
  abstract boolean recursive();
}
````

Now let's add another flag called `--backup` or `-b`, 
and an optional parameter called `--suffix` or `-s`:

````java
@CommandLineArguments
abstract class Args {
  //[...]
  @Parameter(shortName = 'b', longName = "")
  abstract boolean backup();

  @Parameter(shortName = 's')
  abstract Optional<String> suffix();
}
````

Each option can have one of two possible names, or both.
The <em>long name</em> is the name that starts with `--`. 
It can be passed in attached `--key=value` or detached `--key value` form.
The <em>short name</em> starts with a single dash, 
followed by a <em>single</em> character. The attached form is `-kvalue`,
and the detached form is `-k value`.

For each option, the method name 
automatically defines the long name. 
This can be overridden with the `longName` option,
or disabled by passing `longName = ""`.

Each parameter method can also be documented with javadoc.
This will show up in the help text if the user passes `--help`, or, if
the help feature is disabled via `addHelp = false`,
in the parser failure message.

````java
/**
 * Copy SOURCE to DEST
 */
@CommandLineArguments(
    programName = "cp",
    missionStatement = "copy files and directories")
abstract class Args {

  /**
   * Path or file of directory to copy
   *
   * @return SOURCE
   */
  @PositionalParameter
  abstract Path source();

  /**
   * Copy destination
   *
   * @return DEST
   */
  @PositionalParameter
  abstract Path dest();

  /**
   * Copy directories recursively
   */
  @Parameter(shortName = 'r')
  abstract boolean recursive();

  /**
   * Make a backup of each existing destination file
   */
  @Parameter(shortName = 'b', longName = "")
  abstract boolean backup();

  /**
   * Override the usual backup suffix
   */
  @Parameter(shortName = 's')
  abstract Optional<String> suffix();
}
````

When the user passes the special `--help`
parameter on the command line, this program now prints the following:

<pre><code>NAME
       cp - copy files and directories

SYNOPSIS
       cp [&lt;options&gt;] &lt;SOURCE&gt; &lt;DEST&gt;

DESCRIPTION
       Copy SOURCE to DEST

SOURCE
       Path or file of directory to copy

DEST
       Copy destination

OPTIONS
       -r, --recursive
              Copy directories recursively

       -b
              Make a backup of each existing destination file

       -s &lt;suffix&gt;, --suffix &lt;suffix&gt;
              Override the usual backup suffix

       --help
              Print this help page.
              The help flag may only be passed as the first argument.
              Any further arguments will be ignored.
</code></pre>

The full source code of the <em>CopyFile</em>
project can be found
[here](https://github.com/h908714124/CopyFile).

The
[examples](https://github.com/h908714124/jbock/tree/master/examples)
folder has demos and tests for most parser features.

#### Some projects that use jbock?

* [aws-glacier-multipart-upload](https://github.com/h908714124/aws-glacier-multipart-upload)
* [wordlist-extendible](https://github.com/WordListChallenge/wordlist-extendible)
* [jbock-gradle-example](https://github.com/h908714124/jbock-gradle-example)
* ...

## Supported types

There are 4 basic types of parameters, 
depending on the the return type `X` of their parameter method `m`.

* If `X` is of the form `java.util.List<Y>`, then `X` declares a <em>repeatable</em> parameter.
* If `X` is of the form `java.util.Optional<Y>`, then `X` declares an <em>optional, non-repeatable</em> parameter.
* If `m` is not positional and does not have a custom mapper, and `X` is `boolean` or `Boolean`, then `X` declares a <em>non-repeatable flag</em>.
* Otherwise, `X` declares a <em>required, non-repeatable</em> parameter.

If a type `A` is supported, 
then `List<A>` and `Optional<A>` are also supported.
The following types are permissible values of `A`:

* A number of standard classes and primitives are supported out of the box.
See the complete list [here](https://github.com/h908714124/jbock-docgen/blob/master/src/main/java/com/example/helloworld/JbockAllTypes.java).
* Any non-private enum is also supported out of the box. Note, by default this uses the enum's `valueOf` method,
so only the precise enum constant names are understood.
* A custom mapper can be used for almost any `A`.

## Custom mappers

Let `X` be a type.
A <em>mapper class</em> for `X` is any class that implements `Function<String, X>`.

Here's a mapper for `Integer`:

````java
import java.util.function.Function;

class PositiveNumberMapper implements Function<String, Integer> {

  @Override
  public Integer apply(String s) {
    Integer i = Integer.valueOf(s);
    if (i < 0) {
      throw new IllegalArgumentException("The value cannot be negative.");
    }
    return i;
  }
}
````

The `Parameter` and `PositionalParameter` annotations both have an optional `mappedBy` 
attribute, which takes a mapper class:

````java
@Parameter(mappedBy = PositiveNumberMapper.class)
abstract Integer verbosity();
````

#### Rules for mappers

* A mapper for `X` can also be used on a method
that returns `Optional<X>` or `List<X>`.
* The `String` that's passed to the mapper will never be `null`, so there's no need to check for that.
* The mapper may throw any `RuntimeException` to signal a parsing failure.
The generated code will catch it and print the message.
* Even if the mapper returns `null`, the parameter method won't. See pledge above.
* Even with custom mappers, `List` and `Option` are still the <em>only</em> ways to declare non-required arguments.
That means, for instance, that a parameter method that returns 
`Set<Integer>` represents a required (and therefore non-repeatable) argument, 
which is probably not what you want.
The workaround here is to either use `List<Integer>` for a repeatable argument, or 
`Optional<Set<Integer>>` for an optional non-repeatable argument instead. 
