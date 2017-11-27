package net.zerobuilder.examples.gradle;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SimpleCurlArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testNonRepeatable() {
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{}).method())
        .isEqualTo(Optional.empty());
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{"--request="}).method())
        .isEqualTo(Optional.of(""));
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{"--request= "}).method())
        .isEqualTo(Optional.of(" "));
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{"--request", ""}).method())
        .isEqualTo(Optional.of(""));
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{"-X1"}).method())
        .isEqualTo(Optional.of("1"));
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{"-X", "1"}).method())
        .isEqualTo(Optional.of("1"));
  }

  @Test
  public void testRepeatable() {
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{}).headers())
        .isEqualTo(emptyList());
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{"-H1"}).headers())
        .isEqualTo(singletonList("1"));
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{"-H1", "-H2"}).headers())
        .isEqualTo(asList("1", "2"));
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{"-H", "1"}).headers())
        .isEqualTo(singletonList("1"));
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{"-H", "1", "-H", "2"}).headers())
        .isEqualTo(asList("1", "2"));
  }

  @Test
  public void errorInvalidGrouping() {
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{"-vH1"}).urls().size())
        .isEqualTo(1);
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{"-vH1"}).urls().get(0))
        .isEqualTo("-vH1");
  }

  @Test
  public void errorInvalidGroupingLong() {
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{"-vXPOST"}).urls().size())
        .isEqualTo(1);
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{"-vXPOST"}).urls().get(0))
        .isEqualTo("-vXPOST");
  }

  @Test
  public void errorGroupingDuplicateFlag() {
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{"-v", "-vH'Content-Type: application/xml'"}).urls().size())
        .isEqualTo(1);
    assertThat(SimpleCurlArguments_Parser.parse(new String[]{"-v", "-vH'Content-Type: application/xml'"}).urls().get(0))
        .isEqualTo("-vH'Content-Type: application/xml'");
  }

  @Test
  public void errorMissingRepeatable() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing value after token: -H");
    SimpleCurlArguments_Parser.parse(new String[]{"-H"});
  }

  @Test
  public void errorMissingNonRepeatable() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing value after token: --request");
    SimpleCurlArguments_Parser.parse(new String[]{"--request"});
  }

  @Test
  public void errorDuplicateNonRepeatableLong() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: --request, but option METHOD (-X, --request) is not repeatable");
    SimpleCurlArguments_Parser.parse(new String[]{"--request", "GET", "--request", "POST"});
  }

  @Test
  public void errorDuplicateNonRepeatableShort() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: -X2, but option METHOD (-X, --request) is not repeatable");
    SimpleCurlArguments_Parser.parse(new String[]{"-X1", "-X2"});
  }

  @Test
  public void errorDuplicateNonRepeatableLongDetachedShortAttached() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: -X2, but option METHOD (-X, --request) is not repeatable");
    SimpleCurlArguments_Parser.parse(new String[]{"--request", "1", "-X2"});
  }

  @Test
  public void errorDuplicateNonRepeatableLongAttachedShortDetached() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: -X, but option METHOD (-X, --request) is not repeatable");
    SimpleCurlArguments_Parser.parse(new String[]{"--request=1", "-X", "2"});
  }
}
