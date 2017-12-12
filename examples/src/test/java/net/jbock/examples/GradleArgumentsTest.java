package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class GradleArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void errorShortLongConflict() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Option MESSAGE (-m, --message) is not repeatable");
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
    exception.expectMessage("Option MESSAGE (-m, --message) is not repeatable");
    GradleArguments_Parser.parse(new String[]{"--message=hello", "-m", "goodbye"});
  }

  @Test
  public void errorLongLongConflict() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Option MESSAGE (-m, --message) is not repeatable");
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
    exception.expectMessage("Invalid option: 1");
    GradleArguments_Parser.parse(new String[]{"-c1"});
  }

  @Test
  public void errorWeirdOptionGroupEmbeddedHyphen() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid option: -");
    GradleArguments_Parser.parse(new String[]{"-c-v"});
  }

  @Test
  public void errorWeirdOptionGroupTrailingHyphen() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid option: -");
    GradleArguments_Parser.parse(new String[]{"-c-"});
  }

  @Test
  public void errorWeirdOptionGroupEmbeddedEquals() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid option: =");
    GradleArguments_Parser.parse(new String[]{"-c=v"});
  }

  @Test
  public void errorWeirdOptionGroupTrailingEquals() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid option: =");
    GradleArguments_Parser.parse(new String[]{"-c="});
  }

  @Test
  public void errorWeirdOptionGroupAttemptToPassMethod() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid option: X");
    GradleArguments_Parser.parse(new String[]{"-cX=1"});
  }

  @Test
  public void errorInvalidOptionGroupRepeated() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Option '-v' is not repeatable");
    GradleArguments_Parser.parse(new String[]{"-cvv"});
  }

  @Test
  public void errorInvalidOptionGroupUnknownToken() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid option: x");
    GradleArguments_Parser.parse(new String[]{"-cvx"});
  }

  @Test
  public void errorInvalidOptionGroupMissingToken() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid option: m");
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
    assertThat(gradleArguments.otherTokens().size()).isEqualTo(1);
    assertThat(gradleArguments.otherTokens().get(0)).isEqualTo("-");
    assertThat(gradleArguments.ddTokens().size()).isEqualTo(4);
    assertThat(gradleArguments.ddTokens().get(0)).isEqualTo("->");
    assertThat(gradleArguments.ddTokens().get(1)).isEqualTo("<=>");
    assertThat(gradleArguments.ddTokens().get(2)).isEqualTo("");
    assertThat(gradleArguments.ddTokens().get(3)).isEqualTo(" ");
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
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid option: --cmos");
    GradleArguments_Parser.parse(new String[]{"--cmos"});
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
    exception.expectMessage("Invalid option: m");
    GradleArguments_Parser.parse(new String[]{"-cvm", "hello"});
  }

  @Test
  public void testParserForNestedClass() {
    GradleArguments.Foo foo = GradleArguments_Foo_Parser.parse(new String[]{"--bar=4"});
    assertThat(foo.bar()).isEqualTo(Optional.of("4"));
  }
}
