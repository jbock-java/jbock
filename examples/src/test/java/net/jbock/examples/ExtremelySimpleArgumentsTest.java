package net.jbock.examples;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtremelySimpleArgumentsTest {

    @Test
    void simpleTest() {
        ExtremelySimpleArguments result = ExtremelySimpleArgumentsParser.parse(List.of("1"))
                .orElseThrow(f -> new RuntimeException());
        assertEquals(List.of("1"), result.hello());
    }
}
