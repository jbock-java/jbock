package examples.dustin.commandline.jbock;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class MainTest {

    private final ParserTestFixture<Main.Arguments> f =
            ParserTestFixture.create(Main_ArgumentsParser::parse);

    @Test
    void testMain() {
        f.assertThat("-v", "-f", "file.txt")
                .has(Main.Arguments::file, Optional.of("file.txt"))
                .has(Main.Arguments::verbose, true);
    }
}
