package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.RepeatedTest;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class GitArgumentsTest {

  private ParserTestFixture<GitArguments> f =
      ParserTestFixture.create(new GitArguments_Parser());

  @RepeatedTest(10)
  void testEscape() {
    String command = "add";
    String[] randomStrings = randomArgs();
    String[] args = new String[randomStrings.length + 2];
    args[0] = command;
    args[1] = "--";
    System.arraycopy(randomStrings, 0, args, 2, randomStrings.length);
    GitArguments result = f.parse(args);
    String[] remainingArgs = result.remainingArgs().toArray(new String[0]);

    // check that escape sequence works
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
