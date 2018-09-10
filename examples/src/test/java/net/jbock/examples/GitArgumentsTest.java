package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.RepeatedTest;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class GitArgumentsTest {

  private ParserTestFixture<GitArguments> f =
      ParserTestFixture.create(GitArguments_Parser.create());

  @RepeatedTest(10)
  void testRemaining() {
    String command = "add";
    String[] randomStrings = randomArgs();
    String[] args = new String[randomStrings.length + 1];
    args[0] = command;
    System.arraycopy(randomStrings, 0, args, 1, randomStrings.length);
    GitArguments result = f.parse(args);
    String[] remainingArgs = result.remainingArgs();

    // The parser should simply put everything after the command in the list.
    assertArrayEquals(randomStrings, remainingArgs);
  }

  private String[] randomArgs() {
    int n = ThreadLocalRandom.current().nextInt(5);
    String[] result = new String[n];
    for (int i = 0; i < n; i++) {
      result[i] = randomArg();
    }
    return result;
  }

  private String randomArg() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ThreadLocalRandom.current().nextInt(0, 3); i++) {
      int randomInt = ThreadLocalRandom.current().nextInt(10);
      if (randomInt < 5) {
        sb.append("-");
      } else if (randomInt < 9) {
        sb.append("a");
      } else {
        return "--help";
      }
    }
    return sb.toString();
  }
}
