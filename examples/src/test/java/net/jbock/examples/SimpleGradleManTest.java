package net.jbock.examples;

import static net.jbock.examples.SimpleGradleMan_Parser.OptionType.FLAG;
import static net.jbock.examples.SimpleGradleMan_Parser.OptionType.OPTIONAL;
import static net.jbock.examples.SimpleGradleMan_Parser.OptionType.REPEATABLE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SimpleGradleManTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void errorShortLongConflict() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: --message=goodbye, but option MESSAGE (-m, --message) is not repeatable");
    SimpleGradleMan_Parser.parse(new String[]{"-m", "hello", "--message=goodbye"});
  }

  @Test
  public void errorMissingValue() {
    // there's nothing after -m
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing value after token: -m");
    SimpleGradleMan_Parser.parse(new String[]{"-m"});
  }

  @Test
  public void errorLongShortConflict() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: -m, but option MESSAGE (-m, --message) is not repeatable");
    SimpleGradleMan_Parser.parse(new String[]{"--message=hello", "-m", "goodbye"});
  }

  @Test
  public void errorLongLongConflict() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: --message=goodbye, but option MESSAGE (-m, --message) is not repeatable");
    SimpleGradleMan_Parser.parse(new String[]{"--message=hello", "--message=goodbye"});
  }

  @Test
  public void errorNullInArray() {
    exception.expect(NullPointerException.class);
    SimpleGradleMan_Parser.parse(new String[]{null});
  }

  @Test
  public void errorArrayIsNull() {
    exception.expect(NullPointerException.class);
    SimpleGradleMan_Parser.parse(null);
  }

  @Test
  public void errorFlagWithTrailingGarbage() {
    assertThat(SimpleGradleMan_Parser.parse(new String[]{"-c1"}).otherTokens().size())
        .isEqualTo(1);
  }

  @Test
  public void errorWeirdOptionGroupEmbeddedHyphen() {
    assertThat(SimpleGradleMan_Parser.parse(new String[]{"-c-v"}).otherTokens().size())
        .isEqualTo(1);
  }

  @Test
  public void errorWeirdOptionGroupTrailingHyphen() {
    assertThat(SimpleGradleMan_Parser.parse(new String[]{"-c-"}).otherTokens().size())
        .isEqualTo(1);
  }

  @Test
  public void errorWeirdOptionGroupEmbeddedEquals() {
    assertThat(SimpleGradleMan_Parser.parse(new String[]{"-c=v"}).otherTokens().size())
        .isEqualTo(1);
  }

  @Test
  public void errorWeirdOptionGroupTrailingEquals() {
    assertThat(SimpleGradleMan_Parser.parse(new String[]{"-c="}).otherTokens().size())
        .isEqualTo(1);
  }

  @Test
  public void errorWeirdOptionGroupAttemptToPassMethod() {
    assertThat(SimpleGradleMan_Parser.parse(new String[]{"-cX=1"}).otherTokens().size())
        .isEqualTo(1);
  }

  @Test
  public void errorInvalidOptionGroupRepeated() {
    assertThat(SimpleGradleMan_Parser.parse(new String[]{"-cvv"}).otherTokens().size())
        .isEqualTo(1);
  }

  @Test
  public void errorInvalidOptionGroupUnknownToken() {
    assertThat(SimpleGradleMan_Parser.parse(new String[]{"-cvx"}).otherTokens().size())
        .isEqualTo(1);
  }

  @Test
  public void errorInvalidOptionGroupMissingToken() {
    assertThat(SimpleGradleMan_Parser.parse(new String[]{"-cvm"}).otherTokens().size())
        .isEqualTo(1);
    assertThat(SimpleGradleMan_Parser.parse(new String[]{"-cvm"}).otherTokens().get(0))
        .isEqualTo("-cvm");
  }

  @Test
  public void testDetachedLong() {
    SimpleGradleMan gradleMan = SimpleGradleMan_Parser.parse(
        new String[]{"--message", "hello"});
    assertThat(gradleMan.message()).isEqualTo(Optional.of("hello"));
  }

  @Test
  public void testInterestingTokens() {
    SimpleGradleMan gradleMan = SimpleGradleMan_Parser.parse(
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
    assertThat(SimpleGradleMan_Parser.parse(new String[]{"--message="}).message())
        .isEqualTo(Optional.of(""));
    assertThat(SimpleGradleMan_Parser.parse(new String[0]).message())
        .isEqualTo(Optional.empty());
  }

  @Test
  public void testShortNonAtomic() {
    String[] args = {"-m", "hello"};
    SimpleGradleMan gradleMan = SimpleGradleMan_Parser.parse(args);
    assertThat(gradleMan.message()).isEqualTo(Optional.of("hello"));
    assertThat(gradleMan.cmos()).isEqualTo(false);
  }

  @Test
  public void testLongMessage() {
    SimpleGradleMan gradleMan = SimpleGradleMan_Parser.parse(new String[]{"--message=hello"});
    assertThat(gradleMan.message()).isEqualTo(Optional.of("hello"));
    assertThat(gradleMan.cmos()).isEqualTo(false);
  }

  @Test
  public void testShortAtomic() {
    SimpleGradleMan gradleMan = SimpleGradleMan_Parser.parse(new String[]{"-fbar.txt"});
    assertThat(gradleMan.file().size()).isEqualTo(1);
    assertThat(gradleMan.file().get(0)).isEqualTo("bar.txt");
  }

  @Test
  public void testLongShortAtomic() {
    SimpleGradleMan gradleMan = SimpleGradleMan_Parser.parse(new String[]{"--message=hello", "-fbar.txt"});
    assertThat(gradleMan.file().size()).isEqualTo(1);
    assertThat(gradleMan.file().get(0)).isEqualTo("bar.txt");
    assertThat(gradleMan.message()).isEqualTo(Optional.of("hello"));
  }

  @Test
  public void testAttachedFirstToken() {
    SimpleGradleMan gradleMan = SimpleGradleMan_Parser.parse(new String[]{"-fbar.txt", "--message=hello"});
    assertThat(gradleMan.file().size()).isEqualTo(1);
    assertThat(gradleMan.file().get(0)).isEqualTo("bar.txt");
    assertThat(gradleMan.message()).isEqualTo(Optional.of("hello"));
  }

  @Test
  public void testLongSuppressed() {
    // Long option --cmos is suppressed
    SimpleGradleMan gradleMan = SimpleGradleMan_Parser.parse(new String[]{"--cmos"});
    assertThat(gradleMan.cmos()).isEqualTo(false);
    assertThat(gradleMan.otherTokens().size()).isEqualTo(1);
    assertThat(gradleMan.otherTokens().get(0)).isEqualTo("--cmos");
  }

  @Test
  public void testLong() {
    SimpleGradleMan gradleMan = SimpleGradleMan_Parser.parse(new String[]{"--dir=dir"});
    assertThat(gradleMan.dir()).isEqualTo(Optional.of("dir"));
  }

  @Test
  public void testFlag() {
    SimpleGradleMan gradleMan = SimpleGradleMan_Parser.parse(new String[]{"-c", "hello"});
    assertThat(gradleMan.cmos()).isEqualTo(true);
    assertThat(gradleMan.otherTokens().size()).isEqualTo(1);
    assertThat(gradleMan.otherTokens().get(0)).isEqualTo("hello");
  }

  @Test
  public void testNonsense() {
    // bogus options
    SimpleGradleMan gradleMan = SimpleGradleMan_Parser.parse(new String[]{"hello", "goodbye"});
    assertThat(gradleMan.otherTokens().size()).isEqualTo(2);
  }

  @Test
  public void testOptionGroup() {
    assertThat(SimpleGradleMan_Parser.parse(new String[]{"-cv"}).cmos())
        .isFalse();
    assertThat(SimpleGradleMan_Parser.parse(new String[]{"-cv"}).verbose())
        .isFalse();
    assertThat(SimpleGradleMan_Parser.parse(new String[]{"-cv"}).message())
        .isEmpty();
    assertThat(SimpleGradleMan_Parser.parse(new String[]{"-cvm", "a"}).message())
        .isEmpty();
  }

  @Test
  public void testDoubleFlagWithAttachedOption() {
    SimpleGradleMan gradleMan = SimpleGradleMan_Parser.parse(new String[]{"-cvm", "hello"});
    assertThat(gradleMan.otherTokens().size()).isEqualTo(2);
  }

  @Test
  public void testDoubleFlagWithAttachedOptionNoHyphen() {
    SimpleGradleMan gradleMan = SimpleGradleMan_Parser.parse(new String[]{"cvm", "hello"});
    assertThat(gradleMan.cmos()).isEqualTo(false);
    assertThat(gradleMan.verbose()).isEqualTo(false);
    assertThat(gradleMan.otherTokens().size()).isEqualTo(2);
    assertThat(gradleMan.otherTokens().get(0)).isEqualTo("cvm");
    assertThat(gradleMan.otherTokens().get(1)).isEqualTo("hello");
  }

  @Test
  public void testDoubleFlagWithDetachedOption() {
    SimpleGradleMan gradleMan = SimpleGradleMan_Parser.parse(new String[]{"-cvm", "hello"});
    assertThat(gradleMan.cmos()).isFalse();
    assertThat(gradleMan.verbose()).isFalse();
    assertThat(gradleMan.message()).isEmpty();
  }

  @Test
  public void testOptions() {
    assertThat(SimpleGradleMan_Parser.Option.MESSAGE.isSpecial()).isFalse();
    assertThat(SimpleGradleMan_Parser.Option.MESSAGE.isBinding()).isTrue();
    assertThat(SimpleGradleMan_Parser.Option.MESSAGE.type()).isEqualTo(OPTIONAL);
    assertThat(SimpleGradleMan_Parser.Option.MESSAGE.longName()).isEqualTo(Optional.of("message"));
    assertThat(SimpleGradleMan_Parser.Option.MESSAGE.shortName()).isEqualTo(Optional.of('m'));
    assertThat(SimpleGradleMan_Parser.Option.CMOS.isSpecial()).isFalse();
    assertThat(SimpleGradleMan_Parser.Option.CMOS.isBinding()).isFalse();
    assertThat(SimpleGradleMan_Parser.Option.CMOS.type()).isEqualTo(FLAG);
    assertThat(SimpleGradleMan_Parser.Option.CMOS.longName()).isEmpty();
    assertThat(SimpleGradleMan_Parser.Option.CMOS.shortName()).isEqualTo(Optional.of('c'));
    assertThat(SimpleGradleMan_Parser.Option.DIR.isSpecial()).isFalse();
    assertThat(SimpleGradleMan_Parser.Option.DIR.isBinding()).isTrue();
    assertThat(SimpleGradleMan_Parser.Option.DIR.type()).isEqualTo(OPTIONAL);
    assertThat(SimpleGradleMan_Parser.Option.DIR.longName()).isEqualTo(Optional.of("dir"));
    assertThat(SimpleGradleMan_Parser.Option.DIR.shortName()).isEmpty();
    assertThat(SimpleGradleMan_Parser.Option.FILE.isSpecial()).isFalse();
    assertThat(SimpleGradleMan_Parser.Option.FILE.isBinding()).isTrue();
    assertThat(SimpleGradleMan_Parser.Option.FILE.type()).isEqualTo(REPEATABLE);
    assertThat(SimpleGradleMan_Parser.Option.FILE.longName()).isEqualTo(Optional.of("file"));
    assertThat(SimpleGradleMan_Parser.Option.FILE.shortName()).isEqualTo(Optional.of('f'));
    assertThat(SimpleGradleMan_Parser.Option.VERBOSE.isSpecial()).isFalse();
    assertThat(SimpleGradleMan_Parser.Option.VERBOSE.isBinding()).isFalse();
    assertThat(SimpleGradleMan_Parser.Option.VERBOSE.type()).isEqualTo(FLAG);
    assertThat(SimpleGradleMan_Parser.Option.VERBOSE.longName()).isEqualTo(Optional.of("verbose"));
    assertThat(SimpleGradleMan_Parser.Option.VERBOSE.shortName()).isEqualTo(Optional.of('v'));
    assertThat(SimpleGradleMan_Parser.Option.OTHER_TOKENS.isSpecial()).isTrue();
    assertThat(SimpleGradleMan_Parser.Option.OTHER_TOKENS.isBinding()).isFalse();
    assertThat(SimpleGradleMan_Parser.Option.OTHER_TOKENS.type()).isEqualTo(SimpleGradleMan_Parser.OptionType.OTHER_TOKENS);
    assertThat(SimpleGradleMan_Parser.Option.OTHER_TOKENS.longName()).isEmpty();
    assertThat(SimpleGradleMan_Parser.Option.OTHER_TOKENS.shortName()).isEmpty();
    SimpleGradleMan_Parser.Option[] options = SimpleGradleMan_Parser.Option.values();
    assertThat(options.length).isEqualTo(6);
  }

  @Test
  public void testMessageOption() {
    assertThat(SimpleGradleMan_Parser.Option.MESSAGE.description().size()).isEqualTo(2);
    assertThat(SimpleGradleMan_Parser.Option.MESSAGE.description().get(0)).isEqualTo("the message");
    assertThat(SimpleGradleMan_Parser.Option.MESSAGE.description().get(1)).isEqualTo("message goes here");
    assertThat(SimpleGradleMan_Parser.Option.MESSAGE.descriptionArgumentName()).isEqualTo(Optional.of("MESSAGE"));
  }

  @Test
  public void testCmosOption() {
    assertThat(SimpleGradleMan_Parser.Option.CMOS.description().size()).isEqualTo(1);
    assertThat(SimpleGradleMan_Parser.Option.CMOS.description().get(0)).isEqualTo("cmos flag");
    assertThat(SimpleGradleMan_Parser.Option.CMOS.descriptionArgumentName()).isEmpty();
  }

  @Test
  public void testOtherTokensOption() {
    assertThat(SimpleGradleMan_Parser.Option.OTHER_TOKENS.description().size()).isEqualTo(1);
    assertThat(SimpleGradleMan_Parser.Option.OTHER_TOKENS.description().get(0)).isEqualTo("--- description goes here ---");
    assertThat(SimpleGradleMan_Parser.Option.OTHER_TOKENS.descriptionArgumentName()).isEmpty();
  }

  @Test
  public void testPrint() {
    assertThat(SimpleGradleMan_Parser.Option.MESSAGE.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"-m, --message MESSAGE", "  the message", "  message goes here"});
    assertThat(SimpleGradleMan_Parser.Option.FILE.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"-f, --file FILE", "  the files"});
    assertThat(SimpleGradleMan_Parser.Option.DIR.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"--dir DIR", "  the dir"});
    assertThat(SimpleGradleMan_Parser.Option.CMOS.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"-c", "  cmos flag"});
    assertThat(SimpleGradleMan_Parser.Option.VERBOSE.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"-v, --verbose", "  --- description goes here ---"});
    assertThat(SimpleGradleMan_Parser.Option.OTHER_TOKENS.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"Other tokens", "  --- description goes here ---"});
  }
}
