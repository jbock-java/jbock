package net.jbock.annotated;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AnnotatedOptionTest {

    @Test
    void testUnixNamesFirstComparator() {
        assertEquals(List.of("-x", "--a"), Stream.of("--a", "-x")
                .sorted(AnnotatedOption.UNIX_NAMES_FIRST_COMPARATOR)
                .collect(toList()));
        assertEquals(List.of("-a", "-b"), Stream.of("-b", "-a")
                .sorted(AnnotatedOption.UNIX_NAMES_FIRST_COMPARATOR)
                .collect(toList()));
        assertEquals(List.of("--a", "--b"), Stream.of("--b", "--a")
                .sorted(AnnotatedOption.UNIX_NAMES_FIRST_COMPARATOR)
                .collect(toList()));
    }
}