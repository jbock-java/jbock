package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class RestArgumentsTest {

    private final ParserTestFixture<RestArguments> f =
            ParserTestFixture.create(RestArgumentsParser::parse);

    private final Map<String, String> messages = new HashMap<>();

    @BeforeEach
    void setup() {
        messages.put("the.file", "This\nis\ndog\n");
        messages.put("the.rest", "Hello\n   yes\n");
        messages.put("description.main", "A very good program.");
    }

    @Test
    void testNoBundle() {
        f.assertPrintsHelp(
                RestArgumentsParser.createModel(),
                "ouch",
                "",
                "\u001B[1mUSAGE\u001B[m",
                "  rest-arguments [OPTIONS] REST...",
                "",
                "\u001B[1mPARAMETERS\u001B[m",
                "  REST ",
                "",
                "\u001B[1mOPTIONS\u001B[m",
                "  --file FILE  This is the file.",
                "");
    }

    @Test
    void testBundleKey() {
        f.assertPrintsHelp(
                RestArgumentsParser.createModel(),
                messages,
                "A very good program.",
                "",
                "\u001B[1mUSAGE\u001B[m",
                "  rest-arguments [OPTIONS] REST...",
                "",
                "\u001B[1mPARAMETERS\u001B[m",
                "  REST  Hello yes",
                "",
                "\u001B[1mOPTIONS\u001B[m",
                "  --file FILE  This is dog",
                "");
    }
}
