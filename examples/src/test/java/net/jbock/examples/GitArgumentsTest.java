package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GitArgumentsTest {

    private final GitArgumentsParser parser = new GitArgumentsParser();

    private final ParserTestFixture<GitArguments> f =
            ParserTestFixture.create(parser::parse);

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
        assertArrayEquals(randomStrings, remainingArgs, Arrays.toString(args));
    }

    @Test
    void testDoubleEscape() {
        String[] args = {"add", "--", "--", "a"};
        GitArguments result = f.parse(args);
        assertEquals(List.of("--", "a"), result.remainingArgs());
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

    @Test
    void testPrint() {
        f.assertPrintsHelp(
                "Git is software for tracking changes in any set of files.",
                "",
                "\u001B[1mUSAGE\u001B[m",
                "  git-arguments [OPTIONS] COMMAND REMAINING_ARGS...",
                "",
                "\u001B[1mPARAMETERS\u001B[m",
                "  COMMAND         nope",
                "  REMAINING_ARGS  You were a hit! Everyone loves you, now.",
                "",
                "\u001B[1mOPTIONS\u001B[m",
                "  --bare  bear",
                "");
    }
}
