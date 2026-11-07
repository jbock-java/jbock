package net.jbock.contrib;

import net.jbock.util.Either;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

class StandardConvertersTest {

    @Test
    void converterInstancesAreReused() {
        StandardConverters converters = new StandardConverters();
        Function<String, Either<String, Integer>> conv1 = converters.asInteger();
        Function<String, Either<String, Integer>> conv2 = converters.asInteger();
        assertSame(conv1, conv2);
    }

    @Test
    void intConverterWorksAsExpected() {
        Function<String, Either<String, Integer>> conv = new StandardConverters().asInteger();
        Either<String, Integer> result = conv.apply("3");
        result.fold(l -> {
            fail("expecting right");
            return l;
        }, r -> {
            assertEquals(3, r);
            return r;
        });
    }
}
