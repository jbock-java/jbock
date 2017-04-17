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
  public void testCompetingArguments() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Conflicting token: --message=goodbye");
    GradleManParser.parse(new String[]{"-m", "hello", "--message=goodbye"});
  }

  @Test
  public void testCompetingArguments2() {
    // there's nothing after -m, so it is discarded
    GradleMan gradleMan = GradleManParser.parse(new String[]{"--message=hello", "-m"}).bind();
    assertThat(gradleMan.message, is("hello"));
  }

  @Test
  public void testCompetingArguments3() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Conflicting token: -m");
    GradleManParser.parse(new String[]{"--message=hello", "-m", "goodbye"});
  }

  @Test
  public void testDuplicateArguments() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Conflicting token: --message=goodbye");
    GradleManParser.parse(new String[]{"--message=hello", "--message=goodbye"});
  }

  @Test
  public void testShortMessage() {
    GradleManParser parser = GradleManParser.parse(new String[]{"-m", "hello"});
    GradleMan gradleMan = parser.bind();
    assertThat(gradleMan.message, is("hello"));
    assertThat(parser.arguments().size(), is(1));
    assertThat(parser.arguments().get(Option.MESSAGE).token, is("-m"));
    assertThat(parser.arguments().get(Option.MESSAGE).value, is("hello"));
    assertThat(parser.trash().size(), is(0));
  }

  @Test
  public void testLongMessage() {
    GradleManParser parser = GradleManParser.parse(new String[]{"--message=hello"});
    GradleMan gradleMan = parser.bind();
    assertThat(gradleMan.message, is("hello"));
    assertThat(parser.arguments().size(), is(1));
    assertThat(parser.arguments().get(Option.MESSAGE).token, is("--message=hello"));
    assertThat(parser.arguments().get(Option.MESSAGE).value, is("hello"));
    assertThat(parser.trash().size(), is(0));
  }

  @Test
  public void testShortFile() {
    GradleManParser parser = GradleManParser.parse(new String[]{"-f", "file"});
    GradleMan gradleMan = parser.bind();
    assertThat(gradleMan.file, is("file"));
    assertThat(parser.trash().size(), is(0));
  }

  @Test
  public void testLongFile() {
    // --file is invalid
    GradleManParser parser = GradleManParser.parse(new String[]{"--file=file"});
    GradleMan gradleMan = parser.bind();
    assertThat(gradleMan.file, is(nullValue()));
    assertThat(parser.trash().size(), is(1));
    assertThat(parser.trash().get(0), is("--file=file"));
  }

  @Test
  public void testLongDir() {
    GradleManParser parser = GradleManParser.parse(new String[]{"--dir=dir"});
    GradleMan gradleMan = parser.bind();
    assertThat(gradleMan.dir, is("dir"));
    assertThat(parser.trash().size(), is(0));
  }

  @Test
  public void testFlagTrue() {
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
  public void testFlagFalse() {
    // -dir is invalid
    GradleManParser parser = GradleManParser.parse(new String[]{"-dir", "foo"});
    GradleMan gradleMan = parser.bind();
    assertThat(gradleMan.cmos, is(false));
    assertThat(parser.trash().size(), is(2));
    assertThat(parser.trash().get(0), is("-dir"));
    assertThat(parser.trash().get(1), is("foo"));
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
