package net.jbock.convert;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class NamedOptionFactoryTest {

    @Test
    void testUnixNamesFirstComparator() {
        Assertions.assertEquals(List.of("-x", "--a"), Stream.of("--a", "-x")
                .sorted(NamedOptionFactory.UNIX_NAMES_FIRST_COMPARATOR)
                .collect(Collectors.toList()));
        Assertions.assertEquals(List.of("-a", "-b"), Stream.of("-b", "-a")
                .sorted(NamedOptionFactory.UNIX_NAMES_FIRST_COMPARATOR)
                .collect(Collectors.toList()));
        Assertions.assertEquals(List.of("--a", "--b"), Stream.of("--b", "--a")
                .sorted(NamedOptionFactory.UNIX_NAMES_FIRST_COMPARATOR)
                .collect(Collectors.toList()));
    }
}