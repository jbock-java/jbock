## Why jbock

Let's take a look at [jcommander](http://jcommander.org/)
to understand the main reason why jbock was created.

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

This may seem better but now we have introduced a source of `null` values
in our program.

The `argv` array can never contain `null`. It doesn't feel
right to convert it into something that can.

#### It's the wrong type

`verbosity` is an optional parameter, so
we should not be using the types `Integer`
or `int`.

jbock enforces the use of the 
appropriate type, like `Optional<Integer>` 
for an optional parameter. Also, its parameter annotations go on abstract
methods, rather than fields, which means we don't have 
to deal with a field's default value.

If `int` or `Integer` are used as the parameter type,
then jbock will treat that parameter as required.
In either case, jbock will never return `null`
as a parameter value.
