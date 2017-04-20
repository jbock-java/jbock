package net.zerobuilder.examples.gradle;

import net.zerobuilder.examples.gradle.GradleManParser.Option;
import net.zerobuilder.examples.gradle.GradleManParser.OptionType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class GradleManTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testShortLongConflict() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Conflicting token: --message=goodbye");
    GradleManParser.parse(new String[]{"-m", "hello", "--message=goodbye"});
  }

  @Test
  public void testMissingValue() {
    // there's nothing after -m
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing value: -m");
    GradleManParser.parse(new String[]{"-m"}).bind();
  }

  @Test
  public void testLongShortConflict() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Conflicting token: -m");
    GradleManParser.parse(new String[]{"--message=hello", "-m", "goodbye"});
  }

  @Test
  public void testLongLongConflict() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Conflicting token: --message=goodbye");
    GradleManParser.parse(new String[]{"--message=hello", "--message=goodbye"});
  }

  @Test
  public void testShortOptionConfusion() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("The argument to -m may not start with '-', use the long form instead: -f");
    GradleManParser.parse(new String[]{"-m", "-f"});
  }

  @Test
  public void testLongMissingEquals() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing '=' after --message");
    GradleManParser.parse(new String[]{"--message", "hello"});
  }

  @Test
  public void testLongMissingEqualsLastToken() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing '=' after --message");
    GradleManParser.parse(new String[]{"--message"});
  }

  @Test
  public void testLongEmptyString() {
    GradleManParser.Binder parse = GradleManParser.parse(new String[]{"--message="});
    GradleMan gradleMan = parse.bind();
    assertThat(gradleMan.message, is(""));
  }

  @Test
  public void testShortNonAtomic() {
    String[] args = {"-m", "hello"};
    GradleManParser.Binder binder = GradleManParser.parse(args);
    GradleMan gradleMan = binder.bind();
    assertThat(gradleMan.message, is("hello"));
    assertThat(gradleMan.cmos, is(false));
    assertThat(binder.arguments().size(), is(1));
    assertThat(binder.arguments().get(Option.MESSAGE).get(0).reconstruct(), is(args));
    assertThat(binder.arguments().get(Option.MESSAGE).get(0).token, is("-m"));
    assertThat(binder.arguments().get(Option.MESSAGE).get(0).value, is("hello"));
    assertThat(binder.free().size(), is(0));
  }

  @Test
  public void testLongMessage() {
    GradleManParser.Binder binder = GradleManParser.parse(new String[]{"--message=hello"});
    GradleMan gradleMan = binder.bind();
    assertThat(gradleMan.message, is("hello"));
    assertThat(gradleMan.cmos, is(false));
    assertThat(binder.arguments().size(), is(1));
    assertThat(binder.arguments().get(Option.MESSAGE).get(0).token, is("--message=hello"));
    assertThat(binder.arguments().get(Option.MESSAGE).get(0).value, is("hello"));
    assertThat(binder.free().size(), is(0));
  }

  @Test
  public void testShortAtomic() {
    GradleManParser.Binder binder = GradleManParser.parse(new String[]{"-fbar.txt"});
    assertThat(binder.arguments().size(), is(1));
    GradleMan gradleMan = binder.bind();
    assertThat(gradleMan.file.size(), is(1));
    assertThat(gradleMan.file.get(0), is("bar.txt"));
    assertThat(binder.free().size(), is(0));
  }

  @Test
  public void testLongShortAtomic() {
    GradleManParser.Binder binder = GradleManParser.parse(new String[]{"--message=hello", "-fbar.txt"});
    assertThat(binder.arguments().size(), is(2));
    GradleMan gradleMan = binder.bind();
    assertThat(gradleMan.file.size(), is(1));
    assertThat(gradleMan.file.get(0), is("bar.txt"));
    assertThat(gradleMan.message, is("hello"));
    assertThat(binder.free().size(), is(0));
  }

  @Test
  public void testLongInvalid() {
    // --file is not declared
    GradleManParser.Binder binder = GradleManParser.parse(new String[]{"--file=file"});
    GradleMan gradleMan = binder.bind();
    assertThat(gradleMan.file.size(), is(0));
    assertThat(binder.free().size(), is(1));
    assertThat(binder.free().get(0), is("--file=file"));
  }

  @Test
  public void testLong() {
    GradleManParser.Binder binder = GradleManParser.parse(new String[]{"--dir=dir"});
    GradleMan gradleMan = binder.bind();
    assertThat(gradleMan.dir, is("dir"));
    assertThat(binder.free().size(), is(0));
  }

  @Test
  public void testFlag() {
    // -c is a flag; last token goes in the trash
    GradleManParser.Binder binder = GradleManParser.parse(new String[]{"-c", "hello"});
    GradleMan gradleMan = binder.bind();
    assertThat(gradleMan.cmos, is(true));
    assertThat(binder.free().size(), is(1));
    assertThat(binder.free().get(0), is("hello"));
  }

  @Test
  public void testNonsense() {
    // bogus options
    GradleManParser.Binder binder = GradleManParser.parse(new String[]{"hello", "goodbye"});
    assertThat(binder.free().size(), is(2));
  }

  @Test
  public void testOptions() {
    Option[] options = Option.values();
    assertThat(options.length, is(4));
    assertThat(Arrays.stream(options)
            .filter(o -> o.type() != OptionType.FLAG)
            .map(Option::longName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()),
        is(new HashSet<>(asList("message", "dir"))));
    assertThat(Arrays.stream(options)
            .filter(o -> o.type() != OptionType.FLAG)
            .map(Option::shortName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()),
        is(new HashSet<>(asList("f", "m"))));
    assertThat(Arrays.stream(options)
            .filter(o -> o.type() == OptionType.FLAG)
            .map(Option::shortName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()),
        is(new HashSet<>(singletonList("c"))));
  }

  @Test
  public void testMessageOption() {
    assertThat(Option.MESSAGE.description().size(), is(2));
    assertThat(Option.MESSAGE.description().get(0), is("the message"));
    assertThat(Option.MESSAGE.description().get(1), is("message goes here"));
    assertThat(Option.MESSAGE.type(), is(OptionType.STRING));
    assertThat(Option.MESSAGE.longName(), is("message"));
    assertThat(Option.MESSAGE.shortName(), is("m"));
    assertThat(Option.MESSAGE.descriptionParameter(), is("MESSAGE"));
  }

  @Test
  public void testCmosOption() {
    assertThat(Option.CMOS.description().size(), is(1));
    assertThat(Option.CMOS.description().get(0), is("cmos flag"));
    assertThat(Option.CMOS.type(), is(OptionType.FLAG));
    assertThat(Option.CMOS.longName(), is(nullValue()));
    assertThat(Option.CMOS.shortName(), is("c"));
    assertThat(Option.CMOS.descriptionParameter(), is(nullValue()));
  }

  @Test
  public void testParserForNestedClass() {
    GradleMan_FooParser.Binder binder = GradleMan_FooParser.parse(new String[]{"--bar=4"});
    GradleMan.Foo foo = binder.bind();
    assertThat(foo.bar, is("4"));
  }

  @Test
  public void testPrint() {
    Arrays.stream(Option.values())
        .map(o -> o.describe(4))
        .forEach(System.out::println);
  }
}
