package net.jbock.contrib;

import io.jbock.util.Either;
import net.jbock.util.ConverterFailure;
import net.jbock.util.StringConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class StandardConvertersTest {

    @Test
    void converterInstancesAreReused() {
        StringConverter<Integer> conv1 = StandardConverters.asInteger();
        StringConverter<Integer> conv2 = StandardConverters.asInteger();
        assertSame(conv1, conv2);
    }

    @Test
    void intConverterWorksAsExpected() {
        StringConverter<Integer> conv = StandardConverters.asInteger();
        Either<ConverterFailure, Integer> result = conv.apply("3");
        assertEquals(Either.right(3), result);
    }
}