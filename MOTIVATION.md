## Why jbock

Let's take a look at [jcommander](http://jcommander.org/)
to understand my motivation for creating yet another
command line tool.

#### Unintended default values

In jcommander, the parameter annotations go on fields.
Like this:

````java
class Args {

  @Parameter(names = "-v")
  int verbosity;
}
````

and then parsing happens:

````java
Args args = new Args();
String[] argv = {};
JCommander.newBuilder().addObject(args).build().parse(argv);
assertEquals(0, args.verbosity);
````

Because of the implicit default value of `0`,
we cannot tell whether the user passed `{"-v", "0"}` or `{}`.

We could have chosen a different default value, but we can't avoid
having one unless we declare the parameter required.

#### Null to the rescue. Really?

The situation is slightly different if we use an `Integer` instead:

````java
class Args {

  @Parameter(names = "-v")
  Integer verbosity;
}
````

````java
Args args = new Args();
String[] argv = {};
JCommander.newBuilder().addObject(args).build().parse(argv);
assertNull(args.verbosity);
````

Good job! Now we have introduced a source of `null` values
in our program, and we need to remember to check for this later on.
We might also start worrying about input like `{"-v", ""}`,
which doesn't improve the overall mood.

#### Let's do this correctly then.

Part of the problem is that the user hasn't made it very clear 
whether `verbosity` is supposed to be a required parameter or not.

On the one hand, the user chose the types `int` or `Integer`,
rather than `OptionalInt` or `Optional<Integer>`.
Seems like they wanted a required parameter!

But on the other hand, it doesn't make sense to have default
values, like `0` or `null`, unless the parameter is optional.

Jbock builds on Java 8 and enforces the use of the 
appropriate type, like `Optional<Integer>` or `OptionalInt`
for an optional parameter. Its parameter annotations go on abstract
methods, which don't have implicit default values in Java.

If `int` or `Integer` are used as the parameter type,
then jbock will treat that parameter as required. In either case,
jbock will never return `null` or an implicit default value. I like to
think that this behaviour is more "natural".

So that's basically my motivation for writing jbock.
I hope you like it as much as I do.
