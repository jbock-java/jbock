package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimitiveArgumentsTest {

    private final PrimitiveArgumentsParser parser = new PrimitiveArgumentsParser();

    private final ParserTestFixture<PrimitiveArguments> f =
            ParserTestFixture.create(parser::parse);

    @Test
    void simpleTest() {
        PrimitiveArguments parsed = f.parse(
                "-B", "1",
                "-S", "2",
                "-I", "3",
                "-L", "4",
                "-F", "5",
                "-D", "6",
                "-C", "A",
                "-b", "8",
                "-s", "9",
                "-i", "10",
                "-l", "11",
                "-f", "12",
                "-d", "13",
                "-c", "B",
                "-x", "true");
        assertEquals(1, parsed.simpleByte());
        assertEquals(2, parsed.simpleShort());
        assertEquals(3, parsed.simpleInt());
        assertEquals(4, parsed.simpleLong());
        assertEquals(5, parsed.simpleFloat());
        assertEquals(6, parsed.simpleDouble());
        assertEquals('A', parsed.simpleChar());
        assertEquals(8, parsed.mappedByte());
        assertEquals(9, parsed.mappedShort());
        assertEquals(10, parsed.mappedInt());
        assertEquals(11, parsed.mappedLong());
        assertEquals(12, parsed.mappedFloat());
        assertEquals(13, parsed.mappedDouble());
        assertEquals('B', parsed.mappedChar());
        assertTrue(parsed.mappedBoolean());
    }
}