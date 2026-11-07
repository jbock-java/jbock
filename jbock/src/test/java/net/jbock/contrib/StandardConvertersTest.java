package net.jbock.contrib;

import net.jbock.util.Either;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class StandardConvertersTest {

    @Test
    void intConverterWorksAsExpected() {
        Function<String, Either<String, Integer>> conv = StandardConverters::asInteger;
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
