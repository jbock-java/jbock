package examples.dustin.commandline.jbock;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class MainTest {

    private final Main_ArgumentsParser parser = new Main_ArgumentsParser();

    private final ParserTestFixture<Main.Arguments> f =
            ParserTestFixture.create(parser::parse);

    @Test
    void testMain() {
        f.assertThat("-v", "-f", "file.txt")
                .has(Main.Arguments::file, Optional.of("file.txt"))
                .has(Main.Arguments::verbose, true);
    }
}
