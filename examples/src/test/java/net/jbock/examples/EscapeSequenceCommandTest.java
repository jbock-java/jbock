package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EscapeSequenceCommandTest {

    private final ParserTestFixture<EscapeSequenceCommand> f =
            ParserTestFixture.create(EscapeSequenceCommandParser::parse);

    @RepeatedTest(10)
    void randomizedTest() {
        String[] randomStrings = randomWords();
        String[] args = new String[randomStrings.length + 2];
        args[0] = "firstToken";
        args[1] = "--"; // escape sequence
        System.arraycopy(randomStrings, 0, args, 2, randomStrings.length);
        EscapeSequenceCommand result = f.parse(args);
        String[] remainingArgs = result.remainingArgs().toArray(new String[0]);

        // escape sequence works as intended
        assertArrayEquals(randomStrings, remainingArgs);
    }

    @Test
    void testDoubleEscape() {
        String[] args = {"add", "--", "--", "a"};
        EscapeSequenceCommand result = f.parse(args);
        assertEquals(List.of("--", "a"), result.remainingArgs());
    }

    private String[] randomWords() {
        int n = ThreadLocalRandom.current().nextInt(5);
        String[] result = new String[n];
        for (int i = 0; i < n; i++) {
            result[i] = randomWord();
        }
        return result;
    }

    private String randomWord() {
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
