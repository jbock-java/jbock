package net.jbock.examples;

import net.jbock.examples.CustomConverterCommand.MyEnum;
import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomConverterCommandTest {

    private final ParserTestFixture<CustomConverterCommand> f =
            ParserTestFixture.create(CustomConverterCommandParser::parse);

    @Test
    void success() {
        CustomConverterCommand parsed = f.parse(
                "--date", "1500000000000",
                "--optDate", "1500000000000",
                "--dateList", "1500000000000",
                "--verbosity", "0x10",
                "--notFlag", "true",
                "--integerList", "1,2,3,4",
                "--optionalInts", "1",
                "--optionalInts", "",
                "--optionalInts", "3",
                "--listWrapper", "foo",
                "--optionalInts", "4",
                "--enumSet", "FOO",
                "true", "false", "true",
                "--stringArray", "A",
                "--aRequiredInt", "51",
                "--color", "234");
        assertEquals(1500000000000L, parsed.date().getTime());
        assertEquals(Optional.of(1500000000000L), parsed.optDate().map(Date::getTime));
        assertEquals(1500000000000L, parsed.dateList().get(0).getTime());
        assertEquals(Optional.of(16), parsed.verbosity().map(BigInteger::intValue));
        assertEquals(List.of(true, false, true), parsed.booleanList());
        assertEquals(51, parsed.aRequiredInt());
        assertEquals(List.of(1, 2, 3, 4), parsed.integerList().orElseThrow(AssertionFailedError::new));
        assertEquals(List.of(OptionalInt.of(1), OptionalInt.empty(), OptionalInt.of(3), OptionalInt.of(4)),
                parsed.optionalInts());
        assertEquals(singleton(MyEnum.FOO), parsed.enumSet().orElseThrow(AssertionFailedError::new));
        assertEquals(Optional.of(singletonList("foo")), parsed.listWrapper());
        assertArrayEquals(new String[]{"A"}, parsed.stringArray().orElseThrow(AssertionFailedError::new));
        assertTrue(parsed.notFlag());
        assertEquals(Optional.of(new CustomConverterCommand.Color("234")), parsed.color());
    }

    @Test
    void invalidOptions() {
        f.assertThat("--date", "FooBar")
                .fails("while converting option DATE (--date): For input string: \"FooBar\"");
    }
}
