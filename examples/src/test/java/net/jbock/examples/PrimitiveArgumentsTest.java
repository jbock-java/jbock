package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrimitiveArgumentsTest {

    private final ParserTestFixture<PrimitiveArguments> f =
            ParserTestFixture.create(PrimitiveArgumentsParser::parse);

    @Test
    void simpleTest() {
        PrimitiveArguments parsed = f.parse(
                "-B", "1",
                "-S", "2",
                "-I", "3",
                "-L", "4",
                "-F", "5",
                "-D", "6",
                "-C", "A");
        assertEquals(1, parsed.simpleByte());
        assertEquals(2, parsed.simpleShort());
        assertEquals(3, parsed.simpleInt());
        assertEquals(4, parsed.simpleLong());
        assertEquals(5, parsed.simpleFloat());
        assertEquals(6, parsed.simpleDouble());
        assertEquals('A', parsed.simpleChar());
    }
}
