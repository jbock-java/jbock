package net.jbock.annotated;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AnnotatedOptionTest {

    @Test
    void testUnixNamesFirstComparator() {
        Assertions.assertEquals(List.of("-x", "--a"), Stream.of("--a", "-x")
                .sorted(AnnotatedOption.UNIX_NAMES_FIRST_COMPARATOR)
                .collect(Collectors.toList()));
        Assertions.assertEquals(List.of("-a", "-b"), Stream.of("-b", "-a")
                .sorted(AnnotatedOption.UNIX_NAMES_FIRST_COMPARATOR)
                .collect(Collectors.toList()));
        Assertions.assertEquals(List.of("--a", "--b"), Stream.of("--b", "--a")
                .sorted(AnnotatedOption.UNIX_NAMES_FIRST_COMPARATOR)
                .collect(Collectors.toList()));
    }
}