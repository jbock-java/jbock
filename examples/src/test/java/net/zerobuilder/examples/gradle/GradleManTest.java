package net.zerobuilder.examples.gradle;

import net.zerobuilder.examples.gradle.GradleMan_Parser.Option;
import net.zerobuilder.examples.gradle.GradleMan_Parser.OptionType;
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
  public void testShortLongConflict() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Conflicting token: --message=goodbye");
    GradleMan_Parser.parse(new String[]{"-m", "hello", "--message=goodbye"});
  }

  @Test
  public void testMissingValue() throws Exception {
    // there's nothing after -m
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing value: -m");
    GradleMan_Parser.parse(new String[]{"-m"}).bind();
  }

  @Test
  public void testLongShortConflict() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Conflicting token: -m");
    GradleMan_Parser.parse(new String[]{"--message=hello", "-m", "goodbye"});
  }

  @Test
  public void testLongLongConflict() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Conflicting token: --message=goodbye");
    GradleMan_Parser.parse(new String[]{"--message=hello", "--message=goodbye"});
  }

  @Test
  public void testDetachedLong() throws Exception {
    GradleMan gradleMan = GradleMan_Parser.parse(
        new String[]{"--message", "hello"}).bind();
    assertThat(gradleMan.message, is("hello"));
  }

  @Test
  public void testNothing() throws Exception {
    GradleMan gradleMan = GradleMan_Parser.parse(new String[]{}).bind();
    assertThat(gradleMan.message, is(nullValue()));
  }

  @Test
  public void testNull() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("null token");
    GradleMan_Parser.parse(new String[]{null});
  }

  @Test
  public void testInterestingTokens() throws Exception {
    GradleMan_Parser.Binder binder = GradleMan_Parser.parse(
        new String[]{"--message=hello", "-", "--", "->", "<=>", "", " "});
    GradleMan gradleMan = binder.bind();
    assertThat(gradleMan.message, is("hello"));
    assertThat(binder.otherTokens().size(), is(6));
    assertThat(binder.otherTokens().get(0), is("-"));
    assertThat(binder.otherTokens().get(1), is("--"));
    assertThat(binder.otherTokens().get(2), is("->"));
    assertThat(binder.otherTokens().get(3), is("<=>"));
    assertThat(binder.otherTokens().get(4), is(""));
    assertThat(binder.otherTokens().get(5), is(" "));
  }

  @Test
  public void testLongEmptyString() throws Exception {
    GradleMan_Parser.Binder parse = GradleMan_Parser.parse(new String[]{"--message="});
    GradleMan gradleMan = parse.bind();
    assertThat(gradleMan.message, is(""));
  }

  @Test
  public void testShortNonAtomic() throws Exception {
    String[] args = {"-m", "hello"};
    GradleMan_Parser.Binder binder = GradleMan_Parser.parse(args);
    GradleMan gradleMan = binder.bind();
    assertThat(gradleMan.message, is("hello"));
    assertThat(gradleMan.cmos, is(false));
  }

  @Test
  public void testLongMessage() throws Exception {
    GradleMan_Parser.Binder binder = GradleMan_Parser.parse(new String[]{"--message=hello"});
    GradleMan gradleMan = binder.bind();
    assertThat(gradleMan.message, is("hello"));
    assertThat(gradleMan.cmos, is(false));
  }

  @Test
  public void testShortAtomic() throws Exception {
    GradleMan_Parser.Binder binder = GradleMan_Parser.parse(new String[]{"-fbar.txt"});
    GradleMan gradleMan = binder.bind();
    assertThat(gradleMan.file.size(), is(1));
    assertThat(gradleMan.file.get(0), is("bar.txt"));
  }

  @Test
  public void testLongShortAtomic() throws Exception {
    GradleMan_Parser.Binder binder = GradleMan_Parser.parse(new String[]{"--message=hello", "-fbar.txt"});
    GradleMan gradleMan = binder.bind();
    assertThat(gradleMan.file.size(), is(1));
    assertThat(gradleMan.file.get(0), is("bar.txt"));
    assertThat(gradleMan.message, is("hello"));
  }

  @Test
  public void testLongInvalid() throws Exception {
    // --file is not declared
    GradleMan_Parser.Binder binder = GradleMan_Parser.parse(new String[]{"--file=file"});
    GradleMan gradleMan = binder.bind();
    assertThat(gradleMan.file.size(), is(0));
    assertThat(binder.otherTokens().size(), is(1));
    assertThat(binder.otherTokens().get(0), is("--file=file"));
  }

  @Test
  public void testLong() throws Exception {
    GradleMan_Parser.Binder binder = GradleMan_Parser.parse(new String[]{"--dir=dir"});
    GradleMan gradleMan = binder.bind();
    assertThat(gradleMan.dir, is("dir"));
  }

  @Test
  public void testFlag() throws Exception {
    // -c is a flag; last token goes in the trash
    GradleMan_Parser.Binder binder = GradleMan_Parser.parse(new String[]{"-c", "hello"});
    GradleMan gradleMan = binder.bind();
    assertThat(gradleMan.cmos, is(true));
  }

  @Test
  public void testNonsense() throws Exception {
    // bogus options
    GradleMan_Parser.Binder binder = GradleMan_Parser.parse(new String[]{"hello", "goodbye"});
    assertThat(binder.otherTokens().size(), is(2));
  }

  @Test
  public void testOptions() throws Exception {
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
  public void testMessageOption() throws Exception {
    assertThat(Option.MESSAGE.description().size(), is(2));
    assertThat(Option.MESSAGE.description().get(0), is("the message"));
    assertThat(Option.MESSAGE.description().get(1), is("message goes here"));
    assertThat(Option.MESSAGE.type(), is(OptionType.AT_MOST_ONCE));
    assertThat(Option.MESSAGE.longName(), is("message"));
    assertThat(Option.MESSAGE.shortName(), is("m"));
    assertThat(Option.MESSAGE.descriptionParameter(), is("MESSAGE"));
  }

  @Test
  public void testCmosOption() throws Exception {
    assertThat(Option.CMOS.description().size(), is(1));
    assertThat(Option.CMOS.description().get(0), is("cmos flag"));
    assertThat(Option.CMOS.type(), is(OptionType.FLAG));
    assertThat(Option.CMOS.longName(), is(nullValue()));
    assertThat(Option.CMOS.shortName(), is("c"));
    assertThat(Option.CMOS.descriptionParameter(), is(nullValue()));
  }

  @Test
  public void testParserForNestedClass() throws Exception {
    GradleMan_Foo_Parser.Binder binder = GradleMan_Foo_Parser.parse(new String[]{"--bar=4"});
    GradleMan.Foo foo = binder.bind();
    assertThat(foo.bar, is("4"));
  }

  @Test
  public void testPrint() throws Exception {
    Arrays.stream(Option.values())
        .map(o -> o.describe(4))
        .forEach(System.out::println);
  }
}
