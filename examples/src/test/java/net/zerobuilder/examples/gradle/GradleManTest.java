package net.zerobuilder.examples.gradle;

import net.zerobuilder.examples.gradle.GradleManParser.Option;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
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
  public void testNoConflict() {
    // there's nothing after -m, so it goes in the trash, and there's no conflict
    GradleMan gradleMan = GradleManParser.parse(new String[]{"--message=hello", "-m"}).bind();
    assertThat(gradleMan.message, is("hello"));
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
  public void testShortNonAtomic() {
    String[] args = {"-m", "hello"};
    GradleManParser parser = GradleManParser.parse(args);
    GradleMan gradleMan = parser.bind();
    assertThat(gradleMan.message, is("hello"));
    assertThat(gradleMan.cmos, is(false));
    assertThat(parser.arguments().size(), is(1));
    assertThat(parser.arguments().get(Option.MESSAGE).reconstruct(), is(args));
    assertThat(parser.arguments().get(Option.MESSAGE).token, is("-m"));
    assertThat(parser.arguments().get(Option.MESSAGE).value, is("hello"));
    assertThat(parser.trash().size(), is(0));
  }

  @Test
  public void testLongMessage() {
    GradleManParser parser = GradleManParser.parse(new String[]{"--message=hello"});
    GradleMan gradleMan = parser.bind();
    assertThat(gradleMan.message, is("hello"));
    assertThat(gradleMan.cmos, is(false));
    assertThat(parser.arguments().size(), is(1));
    assertThat(parser.arguments().get(Option.MESSAGE).token, is("--message=hello"));
    assertThat(parser.arguments().get(Option.MESSAGE).value, is("hello"));
    assertThat(parser.trash().size(), is(0));
  }

  @Test
  public void testShortAtomic() {
    GradleManParser parser = GradleManParser.parse(new String[]{"-fbar.txt"});
    assertThat(parser.arguments().size(), is(1));
    GradleMan gradleMan = parser.bind();
    assertThat(gradleMan.file, is("bar.txt"));
    assertThat(parser.trash().size(), is(0));
  }

  @Test
  public void testLongShortAtomic() {
    GradleManParser parser = GradleManParser.parse(new String[]{"--message=hello", "-fbar.txt"});
    assertThat(parser.arguments().size(), is(2));
    GradleMan gradleMan = parser.bind();
    assertThat(gradleMan.file, is("bar.txt"));
    assertThat(gradleMan.message, is("hello"));
    assertThat(parser.trash().size(), is(0));
  }

  @Test
  public void testLongInvalid() {
    // --file is not declared
    GradleManParser parser = GradleManParser.parse(new String[]{"--file=file"});
    GradleMan gradleMan = parser.bind();
    assertThat(gradleMan.file, is(nullValue()));
    assertThat(parser.trash().size(), is(1));
    assertThat(parser.trash().get(0), is("--file=file"));
  }

  @Test
  public void testLong() {
    GradleManParser parser = GradleManParser.parse(new String[]{"--dir=dir"});
    GradleMan gradleMan = parser.bind();
    assertThat(gradleMan.dir, is("dir"));
    assertThat(parser.trash().size(), is(0));
  }

  @Test
  public void testFlag() {
    // -c is a flag; last token goes in the trash
    GradleManParser parser = GradleManParser.parse(new String[]{"-c", "hello"});
    GradleMan gradleMan = parser.bind();
    assertThat(gradleMan.cmos, is(true));
    assertThat(parser.trash().size(), is(1));
    assertThat(parser.trash().get(0), is("hello"));
  }

  @Test
  public void testNonsense() {
    // bogus options
    GradleManParser parser = GradleManParser.parse(new String[]{"hello", "goodbye"});
    assertThat(parser.trash().size(), is(2));
  }

  @Test
  public void testOptions() {
    Option[] options = Option.values();
    assertThat(options.length, is(4));
    assertThat(Arrays.stream(options)
            .filter(o -> !o.flag)
            .map(o -> o.longName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()),
        is(new HashSet<>(asList("message", "dir"))));
    assertThat(Arrays.stream(options)
            .filter(o -> !o.flag)
            .map(o -> o.shortName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()),
        is(new HashSet<>(asList("f", "m"))));
    assertThat(Arrays.stream(options)
            .filter(o -> o.flag)
            .map(o -> o.shortName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()),
        is(new HashSet<>(singletonList("c"))));
  }

  @Test
  public void testTrash() {
    // --dir is valid, but there's nothing after it
    GradleManParser parser = GradleManParser.parse(new String[]{"--dir"});
    Map<Option, GradleManParser.Argument> arguments = parser.arguments();
    assertThat(arguments.size(), is(0));
    assertThat(parser.trash().size(), is(1));
    assertThat(parser.trash().get(0), is("--dir"));
  }

  @Test
  public void testMessageOption() {
    assertThat(Option.MESSAGE.description.size(), is(2));
    assertThat(Option.MESSAGE.description.get(0), is("the message"));
    assertThat(Option.MESSAGE.description.get(1), is("message goes here"));
    assertThat(Option.MESSAGE.flag, is(false));
    assertThat(Option.MESSAGE.longName, is("message"));
    assertThat(Option.MESSAGE.shortName, is("m"));
    assertThat(Option.MESSAGE.descriptionParameter, is("MESSAGE"));
  }

  @Test
  public void testCmosOption() {
    assertThat(Option.CMOS.description.size(), is(1));
    assertThat(Option.CMOS.description.get(0), is("cmos flag"));
    assertThat(Option.CMOS.flag, is(true));
    assertThat(Option.CMOS.longName, is(nullValue()));
    assertThat(Option.CMOS.shortName, is("c"));
    assertThat(Option.CMOS.descriptionParameter, is(nullValue()));
  }

  @Test
  public void testNesting() {
    GradleMan_FooParser parser = GradleMan_FooParser.parse(new String[]{"--bar=4"});
    GradleMan.Foo foo = parser.bind();
    assertThat(foo.bar, is("4"));
  }

  @Test
  public void testPrint() {
    Arrays.stream(Option.values())
        .map(o -> o.describe(4))
        .forEach(System.out::println);
  }
}
