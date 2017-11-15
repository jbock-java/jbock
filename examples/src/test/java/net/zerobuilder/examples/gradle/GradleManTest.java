package net.zerobuilder.examples.gradle;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.zerobuilder.examples.gradle.GradleMan_Parser.Option;
import net.zerobuilder.examples.gradle.GradleMan_Parser.OptionType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class GradleManTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void errorShortLongConflict() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Conflicting token: --message=goodbye");
    GradleMan_Parser.parse(new String[]{"-m", "hello", "--message=goodbye"});
  }

  @Test
  public void errorMissingValue() {
    // there's nothing after -m
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing value: -m");
    GradleMan_Parser.parse(new String[]{"-m"});
  }

  @Test
  public void errorLongShortConflict() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Conflicting token: -m");
    GradleMan_Parser.parse(new String[]{"--message=hello", "-m", "goodbye"});
  }

  @Test
  public void errorLongLongConflict() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Conflicting token: --message=goodbye");
    GradleMan_Parser.parse(new String[]{"--message=hello", "--message=goodbye"});
  }

  @Test
  public void errorNull() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("null token");
    GradleMan_Parser.parse(new String[]{null});
  }

  @Test
  public void errorFlagWithTrailingGarbage() {
    exception.expect(IllegalArgumentException.class);
    GradleMan_Parser.parse(new String[]{"-c1"});
  }

  @Test
  public void testDetachedLong() {
    GradleMan gradleMan = GradleMan_Parser.parse(
        new String[]{"--message", "hello"});
    assertThat(gradleMan.message()).isEqualTo(Optional.of("hello"));
  }

  @Test
  public void testInterestingTokens() {
    GradleMan gradleMan = GradleMan_Parser.parse(
        new String[]{"--message=hello", "-", "--", "->", "<=>", "", " "});
    assertThat(gradleMan.message()).isEqualTo(Optional.of("hello"));
    assertThat(gradleMan.otherTokens().size()).isEqualTo(6);
    assertThat(gradleMan.otherTokens().get(0)).isEqualTo("-");
    assertThat(gradleMan.otherTokens().get(1)).isEqualTo("--");
    assertThat(gradleMan.otherTokens().get(2)).isEqualTo("->");
    assertThat(gradleMan.otherTokens().get(3)).isEqualTo("<=>");
    assertThat(gradleMan.otherTokens().get(4)).isEqualTo("");
    assertThat(gradleMan.otherTokens().get(5)).isEqualTo(" ");
  }

  @Test
  public void testEmptyVersusAbsent() {
    assertThat(GradleMan_Parser.parse(new String[]{"--message="}).message())
        .isEqualTo(Optional.of(""));
    assertThat(GradleMan_Parser.parse(new String[0]).message())
        .isEqualTo(Optional.empty());
  }

  @Test
  public void testShortNonAtomic() {
    String[] args = {"-m", "hello"};
    GradleMan gradleMan = GradleMan_Parser.parse(args);
    assertThat(gradleMan.message()).isEqualTo(Optional.of("hello"));
    assertThat(gradleMan.cmos()).isEqualTo(false);
  }

  @Test
  public void testLongMessage() {
    GradleMan gradleMan = GradleMan_Parser.parse(new String[]{"--message=hello"});
    assertThat(gradleMan.message()).isEqualTo(Optional.of("hello"));
    assertThat(gradleMan.cmos()).isEqualTo(false);
  }

  @Test
  public void testShortAtomic() {
    GradleMan gradleMan = GradleMan_Parser.parse(new String[]{"-fbar.txt"});
    assertThat(gradleMan.file().size()).isEqualTo(1);
    assertThat(gradleMan.file().get(0)).isEqualTo("bar.txt");
  }

  @Test
  public void testLongShortAtomic() {
    GradleMan gradleMan = GradleMan_Parser.parse(new String[]{"--message=hello", "-fbar.txt"});
    assertThat(gradleMan.file().size()).isEqualTo(1);
    assertThat(gradleMan.file().get(0)).isEqualTo("bar.txt");
    assertThat(gradleMan.message()).isEqualTo(Optional.of("hello"));
  }

  @Test
  public void testLongSuppressed() {
    // Long option --cmos is suppressed
    GradleMan gradleMan = GradleMan_Parser.parse(new String[]{"--cmos"});
    assertThat(gradleMan.cmos()).isEqualTo(false);
    assertThat(gradleMan.otherTokens().size()).isEqualTo(1);
    assertThat(gradleMan.otherTokens().get(0)).isEqualTo("--cmos");
  }

  @Test
  public void testLong() {
    GradleMan gradleMan = GradleMan_Parser.parse(new String[]{"--dir=dir"});
    assertThat(gradleMan.dir()).isEqualTo(Optional.of("dir"));
  }

  @Test
  public void testFlag() {
    // -c is a flag; last token goes in the trash
    GradleMan gradleMan = GradleMan_Parser.parse(new String[]{"-c", "hello"});
    assertThat(gradleMan.cmos()).isEqualTo(true);
  }

  @Test
  public void testNonsense() {
    // bogus options
    GradleMan gradleMan = GradleMan_Parser.parse(new String[]{"hello", "goodbye"});
    assertThat(gradleMan.otherTokens().size()).isEqualTo(2);
  }

  @Test
  public void testDoubleFlag() {
    GradleMan gradleMan = GradleMan_Parser.parse(new String[]{"-cv"});
    assertThat(gradleMan.cmos()).isEqualTo(true);
    assertThat(gradleMan.verbose()).isEqualTo(true);
  }

  @Test
  public void testDoubleFlagWithAttachedOption() {
    GradleMan gradleMan = GradleMan_Parser.parse(new String[]{"-cvmhello"});
    assertThat(gradleMan.cmos()).isEqualTo(true);
    assertThat(gradleMan.verbose()).isEqualTo(true);
    assertThat(gradleMan.message()).isEqualTo(Optional.of("hello"));
  }

  @Test
  public void testDoubleFlagWithDetachedOption() {
    GradleMan gradleMan = GradleMan_Parser.parse(new String[]{"-cvm", "hello"});
    assertThat(gradleMan.cmos()).isEqualTo(true);
    assertThat(gradleMan.verbose()).isEqualTo(true);
    assertThat(gradleMan.message()).isEqualTo(Optional.of("hello"));
  }

  @Test
  public void testOptions() {
    Option[] options = Option.values();
    assertThat(options.length).isEqualTo(6);
    assertThat(Arrays.stream(options)
        .filter(o -> o.type() != OptionType.FLAG)
        .map(Option::longName)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet()))
        .isEqualTo(new HashSet<>(asList("file", "otherTokens", "message", "dir")));
    assertThat(Arrays.stream(options)
        .filter(o -> o.type() != OptionType.FLAG)
        .map(Option::shortName)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet()))
        .isEqualTo(new HashSet<>(asList("f", "m")));
    assertThat(Arrays.stream(options)
        .filter(o -> o.type() == OptionType.FLAG)
        .map(Option::shortName)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet()))
        .isEqualTo(new HashSet<>(asList("c", "v")));
  }

  @Test
  public void testMessageOption() {
    assertThat(Option.MESSAGE.description().size()).isEqualTo(2);
    assertThat(Option.MESSAGE.description().get(0)).isEqualTo("the message");
    assertThat(Option.MESSAGE.description().get(1)).isEqualTo("message goes here");
    assertThat(Option.MESSAGE.type()).isEqualTo(OptionType.OPTIONAL);
    assertThat(Option.MESSAGE.longName()).isEqualTo("message");
    assertThat(Option.MESSAGE.shortName()).isEqualTo("m");
    assertThat(Option.MESSAGE.descriptionParameter()).isEqualTo("MESSAGE");
  }

  @Test
  public void testCmosOption() {
    assertThat(Option.CMOS.description().size()).isEqualTo(1);
    assertThat(Option.CMOS.description().get(0)).isEqualTo("cmos flag");
    assertThat(Option.CMOS.type()).isEqualTo(OptionType.FLAG);
    assertThat(Option.CMOS.longName()).isNull();
    assertThat(Option.CMOS.shortName()).isEqualTo("c");
    assertThat(Option.CMOS.descriptionParameter()).isNull();
  }

  @Test
  public void testParserForNestedClass() {
    GradleMan.Foo foo = GradleMan_Foo_Parser.parse(new String[]{"--bar=4"});
    assertThat(foo.bar()).isEqualTo(Optional.of("4"));
  }

  @Test
  public void testPrint() {
    assertThat(Option.MESSAGE.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"-m, --message=MESSAGE", "  the message", "  message goes here"});
    assertThat(Option.FILE.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"-f, --file=FILE", "  the files"});
    assertThat(Option.DIR.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"--dir=DIR", "  the dir"});
    assertThat(Option.CMOS.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"-c", "  cmos flag"});
    assertThat(Option.VERBOSE.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"-v, --verbose", "  --- description goes here ---"});
    assertThat(Option.OTHER_TOKENS.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"[otherTokens]", "  --- description goes here ---"});
  }
}
