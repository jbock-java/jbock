package net.jbock.examples;

import io.jbock.util.Either;
import net.jbock.examples.fixture.ParserTestFixture;
import net.jbock.util.ConverterReturnedNull;
import net.jbock.util.ErrConvert;
import net.jbock.util.ParsingFailed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(parsed.getLeft().isPresent());
        ParsingFailed parsingFailed = parsed.getLeft().get();
        assertTrue(parsingFailed instanceof ErrConvert);
        ErrConvert errConvert = (ErrConvert) parsingFailed;
        assertTrue(errConvert.converterFailure() instanceof ConverterReturnedNull);
    }
}