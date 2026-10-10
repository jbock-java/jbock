package net.jbock.contrib;

import net.jbock.util.ConverterFailure;
import net.jbock.util.Either;
import net.jbock.util.StringConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

class StandardConvertersTest {

    @Test
    void converterInstancesAreReused() {
        StandardConverters converters = new StandardConverters();
        StringConverter<Integer> conv1 = converters.asInteger();
        StringConverter<Integer> conv2 = converters.asInteger();
        assertSame(conv1, conv2);
    }

    @Test
    void intConverterWorksAsExpected() {
        StringConverter<Integer> conv = new StandardConverters().asInteger();
        Either<ConverterFailure, Integer> result = conv.apply("3");
        result.fold(l -> {
            fail("expecting right");
            return l;
        }, r -> {
            assertEquals(3, r);
            return r;
        });
    }
}
