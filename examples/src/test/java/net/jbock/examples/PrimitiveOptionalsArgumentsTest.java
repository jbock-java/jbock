package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrimitiveOptionalsArgumentsTest {

    private final PrimitiveOptionalsArgumentsParser parser = new PrimitiveOptionalsArgumentsParser();

    private final ParserTestFixture<PrimitiveOptionalsArguments> f =
            ParserTestFixture.create(parser::parse);

    @Test
    void simpleTest() {
        PrimitiveOptionalsArguments parsed = f.parse(
                "-I", "1",
                "-L", "2",
                "-D", "3",
                "-i", "4",
                "-l", "5",
                "-d", "6");
        assertEquals(OptionalInt.of(1), parsed.simpleInt());
        assertEquals(OptionalLong.of(2), parsed.simpleLong());
        assertEquals(OptionalDouble.of(3), parsed.simpleDouble());
        assertEquals(OptionalInt.of(4), parsed.mappedInt());
        assertEquals(OptionalLong.of(5), parsed.mappedLong());
        assertEquals(OptionalDouble.of(6), parsed.mappedDouble());
    }
}