package net.jbock.examples;

import static net.jbock.examples.GradleArguments_Parser.OptionType.FLAG;
import static net.jbock.examples.GradleArguments_Parser.OptionType.OPTIONAL;
import static net.jbock.examples.GradleArguments_Parser.OptionType.REPEATABLE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import net.jbock.examples.GradleArguments_Parser.Option;
import net.jbock.examples.GradleArguments_Parser.OptionType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class GradleArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void errorShortLongConflict() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: --message=goodbye, but option MESSAGE (-m, --message) is not repeatable");
    GradleArguments_Parser.parse(new String[]{"-m", "hello", "--message=goodbye"});
  }

  @Test
  public void errorMissingValue() {
    // there's nothing after -m
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing value after token: -m");
    GradleArguments_Parser.parse(new String[]{"-m"});
  }

  @Test
  public void errorLongShortConflict() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: -m, but option MESSAGE (-m, --message) is not repeatable");
    GradleArguments_Parser.parse(new String[]{"--message=hello", "-m", "goodbye"});
  }

  @Test
  public void errorLongLongConflict() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: --message=goodbye, but option MESSAGE (-m, --message) is not repeatable");
    GradleArguments_Parser.parse(new String[]{"--message=hello", "--message=goodbye"});
  }

  @Test
  public void errorNullInArray() {
    exception.expect(NullPointerException.class);
    GradleArguments_Parser.parse(new String[]{null});
  }

  @Test
  public void errorArrayIsNull() {
    exception.expect(NullPointerException.class);
    GradleArguments_Parser.parse(null);
  }

  @Test
  public void errorFlagWithTrailingGarbage() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid token in option group '-c1': '1'");
    GradleArguments_Parser.parse(new String[]{"-c1"});
  }

  @Test
  public void errorWeirdOptionGroupEmbeddedHyphen() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid token in option group '-c-v': '-'");
    GradleArguments_Parser.parse(new String[]{"-c-v"});
  }

  @Test
  public void errorWeirdOptionGroupTrailingHyphen() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid token in option group '-c-': '-'");
    GradleArguments_Parser.parse(new String[]{"-c-"});
  }

  @Test
  public void errorWeirdOptionGroupEmbeddedEquals() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid token in option group '-c=v': '='");
    GradleArguments_Parser.parse(new String[]{"-c=v"});
  }

  @Test
  public void errorWeirdOptionGroupTrailingEquals() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid token in option group '-c=': '='");
    GradleArguments_Parser.parse(new String[]{"-c="});
  }

  @Test
  public void errorWeirdOptionGroupAttemptToPassMethod() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid token in option group '-cX=1': 'X'");
    GradleArguments_Parser.parse(new String[]{"-cX=1"});
  }

  @Test
  public void errorInvalidOptionGroupRepeated() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("In option group '-cvv': option 'v' is not repeatable");
    GradleArguments_Parser.parse(new String[]{"-cvv"});
  }

  @Test
  public void errorInvalidOptionGroupUnknownToken() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid token in option group '-cvx': 'x'");
    GradleArguments_Parser.parse(new String[]{"-cvx"});
  }

  @Test
  public void errorInvalidOptionGroupMissingToken() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid token in option group '-cvm': 'm'");
    GradleArguments_Parser.parse(new String[]{"-cvm"});
  }

  @Test
  public void testDetachedLong() {
    GradleArguments gradleArguments = GradleArguments_Parser.parse(
        new String[]{"--message", "hello"});
    assertThat(gradleArguments.message()).isEqualTo(Optional.of("hello"));
  }

  @Test
  public void testInterestingTokens() {
    GradleArguments gradleArguments = GradleArguments_Parser.parse(
        new String[]{"--message=hello", "-", "--", "->", "<=>", "", " "});
    assertThat(gradleArguments.message()).isEqualTo(Optional.of("hello"));
    assertThat(gradleArguments.otherTokens().size()).isEqualTo(6);
    assertThat(gradleArguments.otherTokens().get(0)).isEqualTo("-");
    assertThat(gradleArguments.otherTokens().get(1)).isEqualTo("--");
    assertThat(gradleArguments.otherTokens().get(2)).isEqualTo("->");
    assertThat(gradleArguments.otherTokens().get(3)).isEqualTo("<=>");
    assertThat(gradleArguments.otherTokens().get(4)).isEqualTo("");
    assertThat(gradleArguments.otherTokens().get(5)).isEqualTo(" ");
  }

  @Test
  public void testEmptyVersusAbsent() {
    assertThat(GradleArguments_Parser.parse(new String[]{"--message="}).message())
        .isEqualTo(Optional.of(""));
    assertThat(GradleArguments_Parser.parse(new String[0]).message())
        .isEqualTo(Optional.empty());
  }

  @Test
  public void testShortNonAtomic() {
    String[] args = {"-m", "hello"};
    GradleArguments gradleArguments = GradleArguments_Parser.parse(args);
    assertThat(gradleArguments.message()).isEqualTo(Optional.of("hello"));
    assertThat(gradleArguments.cmos()).isEqualTo(false);
  }

  @Test
  public void testLongMessage() {
    GradleArguments gradleArguments = GradleArguments_Parser.parse(new String[]{"--message=hello"});
    assertThat(gradleArguments.message()).isEqualTo(Optional.of("hello"));
    assertThat(gradleArguments.cmos()).isEqualTo(false);
  }

  @Test
  public void testShortAtomic() {
    GradleArguments gradleArguments = GradleArguments_Parser.parse(new String[]{"-fbar.txt"});
    assertThat(gradleArguments.file().size()).isEqualTo(1);
    assertThat(gradleArguments.file().get(0)).isEqualTo("bar.txt");
  }

  @Test
  public void testLongShortAtomic() {
    GradleArguments gradleArguments = GradleArguments_Parser.parse(new String[]{"--message=hello", "-fbar.txt"});
    assertThat(gradleArguments.file().size()).isEqualTo(1);
    assertThat(gradleArguments.file().get(0)).isEqualTo("bar.txt");
    assertThat(gradleArguments.message()).isEqualTo(Optional.of("hello"));
  }

  @Test
  public void testAttachedFirstToken() {
    GradleArguments gradleArguments = GradleArguments_Parser.parse(new String[]{"-fbar.txt", "--message=hello"});
    assertThat(gradleArguments.file().size()).isEqualTo(1);
    assertThat(gradleArguments.file().get(0)).isEqualTo("bar.txt");
    assertThat(gradleArguments.message()).isEqualTo(Optional.of("hello"));
  }

  @Test
  public void testLongSuppressed() {
    // Long option --cmos is suppressed
    GradleArguments gradleArguments = GradleArguments_Parser.parse(new String[]{"--cmos"});
    assertThat(gradleArguments.cmos()).isEqualTo(false);
    assertThat(gradleArguments.otherTokens().size()).isEqualTo(1);
    assertThat(gradleArguments.otherTokens().get(0)).isEqualTo("--cmos");
  }

  @Test
  public void testLong() {
    GradleArguments gradleArguments = GradleArguments_Parser.parse(new String[]{"--dir=dir"});
    assertThat(gradleArguments.dir()).isEqualTo(Optional.of("dir"));
  }

  @Test
  public void testFlag() {
    GradleArguments gradleArguments = GradleArguments_Parser.parse(new String[]{"-c", "hello"});
    assertThat(gradleArguments.cmos()).isEqualTo(true);
    assertThat(gradleArguments.otherTokens().size()).isEqualTo(1);
    assertThat(gradleArguments.otherTokens().get(0)).isEqualTo("hello");
  }

  @Test
  public void testNonsense() {
    // bogus options
    GradleArguments gradleArguments = GradleArguments_Parser.parse(new String[]{"hello", "goodbye"});
    assertThat(gradleArguments.otherTokens().size()).isEqualTo(2);
  }

  @Test
  public void testOptionGroup() {
    assertThat(GradleArguments_Parser.parse(new String[]{"-cv"}).cmos())
        .isTrue();
    assertThat(GradleArguments_Parser.parse(new String[]{"-cv"}).verbose())
        .isTrue();
    assertThat(GradleArguments_Parser.parse(new String[]{"-cv"}).message())
        .isEqualTo(Optional.empty());
  }

  @Test
  public void errorDoubleFlagWithAttachedOption() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid token in option group '-cvm': 'm'");
    GradleArguments_Parser.parse(new String[]{"-cvm", "hello"});
  }

  @Test
  public void testOptions() {
    assertThat(Option.MESSAGE.isSpecial()).isFalse();
    assertThat(Option.MESSAGE.isBinding()).isTrue();
    assertThat(Option.MESSAGE.type()).isEqualTo(OPTIONAL);
    assertThat(Option.MESSAGE.longName()).isEqualTo(Optional.of("message"));
    assertThat(Option.MESSAGE.shortName()).isEqualTo(Optional.of('m'));
    assertThat(Option.CMOS.isSpecial()).isFalse();
    assertThat(Option.CMOS.isBinding()).isFalse();
    assertThat(Option.CMOS.type()).isEqualTo(FLAG);
    assertThat(Option.CMOS.longName()).isEmpty();
    assertThat(Option.CMOS.shortName()).isEqualTo(Optional.of('c'));
    assertThat(Option.DIR.isSpecial()).isFalse();
    assertThat(Option.DIR.isBinding()).isTrue();
    assertThat(Option.DIR.type()).isEqualTo(OPTIONAL);
    assertThat(Option.DIR.longName()).isEqualTo(Optional.of("dir"));
    assertThat(Option.DIR.shortName()).isEmpty();
    assertThat(Option.FILE.isSpecial()).isFalse();
    assertThat(Option.FILE.isBinding()).isTrue();
    assertThat(Option.FILE.type()).isEqualTo(REPEATABLE);
    assertThat(Option.FILE.longName()).isEqualTo(Optional.of("file"));
    assertThat(Option.FILE.shortName()).isEqualTo(Optional.of('f'));
    assertThat(Option.VERBOSE.isSpecial()).isFalse();
    assertThat(Option.VERBOSE.isBinding()).isFalse();
    assertThat(Option.VERBOSE.type()).isEqualTo(FLAG);
    assertThat(Option.VERBOSE.longName()).isEqualTo(Optional.of("verbose"));
    assertThat(Option.VERBOSE.shortName()).isEqualTo(Optional.of('v'));
    assertThat(Option.OTHER_TOKENS.isSpecial()).isTrue();
    assertThat(Option.OTHER_TOKENS.isBinding()).isFalse();
    assertThat(Option.OTHER_TOKENS.type()).isEqualTo(OptionType.OTHER_TOKENS);
    assertThat(Option.OTHER_TOKENS.longName()).isEmpty();
    assertThat(Option.OTHER_TOKENS.shortName()).isEmpty();
    Option[] options = Option.values();
    assertThat(options.length).isEqualTo(6);
  }

  @Test
  public void testMessageOption() {
    assertThat(Option.MESSAGE.description().size()).isEqualTo(2);
    assertThat(Option.MESSAGE.description().get(0)).isEqualTo("the message");
    assertThat(Option.MESSAGE.description().get(1)).isEqualTo("message goes here");
    assertThat(Option.MESSAGE.descriptionArgumentName()).isEqualTo(Optional.of("MESSAGE"));
  }

  @Test
  public void testCmosOption() {
    assertThat(Option.CMOS.description().size()).isEqualTo(1);
    assertThat(Option.CMOS.description().get(0)).isEqualTo("cmos flag");
    assertThat(Option.CMOS.descriptionArgumentName()).isEmpty();
  }

  @Test
  public void testOtherTokensOption() {
    assertThat(Option.OTHER_TOKENS.description().size()).isEqualTo(1);
    assertThat(Option.OTHER_TOKENS.description().get(0)).isEqualTo("--- description goes here ---");
    assertThat(Option.OTHER_TOKENS.descriptionArgumentName()).isEmpty();
  }

  @Test
  public void testParserForNestedClass() {
    GradleArguments.Foo foo = GradleArguments_Foo_Parser.parse(new String[]{"--bar=4"});
    assertThat(foo.bar()).isEqualTo(Optional.of("4"));
  }

  @Test
  public void testPrint() {
    assertThat(Option.MESSAGE.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"-m, --message MESSAGE", "  the message", "  message goes here"});
    assertThat(Option.FILE.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"-f, --file FILE", "  the files"});
    assertThat(Option.DIR.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"--dir DIR", "  the dir"});
    assertThat(Option.CMOS.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"-c", "  cmos flag"});
    assertThat(Option.VERBOSE.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"-v, --verbose", "  --- description goes here ---"});
    assertThat(Option.OTHER_TOKENS.describe(2).split("\n", -1))
        .isEqualTo(new String[]{"Other tokens", "  --- description goes here ---"});
  }
}
