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
                "USAGE",
                "  rest-arguments [OPTIONS] REST...",
                "",
                "PARAMETERS",
                "  REST ",
                "",
                "OPTIONS",
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
                "USAGE",
                "  rest-arguments [OPTIONS] REST...",
                "",
                "PARAMETERS",
                "  REST  Hello yes",
                "",
                "OPTIONS",
                "  --file FILE  This is dog",
                "");
    }
}
