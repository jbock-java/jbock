package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.ConverterReturnedNull;
import net.jbock.util.Either;
import net.jbock.util.ErrConvert;
import net.jbock.util.ParsingFailed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class ComplicatedMapperArgumentsTest {

    private final ParserTestFixture<ComplicatedMapperArguments> f =
            ParserTestFixture.create(ComplicatedMapperArgumentsParser::parse);

    @Test
    void lazyNumber() {
        ComplicatedMapperArguments parsed = f.parse(
                "-N", "12",
                "--numbers", "3",
                "--numbers", "oops");
        assertEquals(1, parsed.number().intValue());
        assertEquals(2, parsed.numbers().size());
        assertEquals(Integer.valueOf(3), parsed.numbers().get(0).get());
        assertThrows(NumberFormatException.class, () -> parsed.numbers().get(1).get());
    }

    @Test
    void nullConverter() {
        Either<ParsingFailed, ComplicatedMapperArguments> parsed = ComplicatedMapperArgumentsParser.parse(List.of(
                "-N", "12",
                "--date", "2020-01-10"));
        parsed.fold(l -> {
            assertInstanceOf(ErrConvert.class, l);
            ErrConvert errConvert = (ErrConvert) l;
            assertInstanceOf(ConverterReturnedNull.class, errConvert.converterFailure());
            return l;
        }, r -> {
            fail("left expected");
            return r;
        });
    }
}
