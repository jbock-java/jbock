package net.jbock.examples;

import net.jbock.examples.CustomMapperArguments.MyEnum;
import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.OptionalInt;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomMapperArgumentsTest {

  private ParserTestFixture<CustomMapperArguments> f =
      ParserTestFixture.create(CustomMapperArguments_Parser.create());

  @Test
  void success() {
    CustomMapperArguments parsed = f.parse(
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
        "--aRequiredInt", "51");
    assertEquals(1500000000000L, parsed.date().getTime());
    assertEquals(Optional.of(1500000000000L), parsed.optDate().map(Date::getTime));
    assertEquals(1500000000000L, parsed.dateList().get(0).getTime());
    assertEquals(Optional.of(16), parsed.verbosity().map(BigInteger::intValue));
    assertEquals(Arrays.asList(true, false, true), parsed.booleanList());
    assertEquals(51, parsed.aRequiredInt());
    assertEquals(Arrays.asList(1, 2, 3, 4), parsed.integerList().orElseThrow(AssertionFailedError::new));
    assertEquals(Arrays.asList(OptionalInt.of(1), OptionalInt.empty(), OptionalInt.of(3), OptionalInt.of(4)),
        parsed.optionalInts());
    assertEquals(singleton(MyEnum.FOO), parsed.enumSet().orElseThrow(AssertionFailedError::new));
    assertEquals(Optional.of(singletonList("foo")), parsed.listWrapper());
    assertArrayEquals(new String[]{"A"}, parsed.stringArray().orElseThrow(AssertionFailedError::new));
    assertTrue(parsed.notFlag());
  }

  @Test
  void invalidOptions() {
    f.assertThat("--date", "FooBar").failsWithUsageMessage("For input string: \"FooBar\"");
    f.assertThat().failsWithUsageMessage("Missing required option: DATE (--date)");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "NAME",
        "  CustomMapperArguments",
        "",
        "SYNOPSIS",
        "  CustomMapperArguments [<options>] --date=<DATE> --aRequiredInt=<A_REQUIRED_INT> --notFlag=<NOT_FLAG> [<boolean_list...>]",
        "",
        "DESCRIPTION",
        "",
        "BOOLEAN_LIST",
        "",
        "OPTIONS",
        "  --date <DATE>",
        "    The mapper must be a Function from String to whatever-this-returns.",
        "    It must also have a package-visible no-arg constructor.",
        "",
        "  --optDate <opt_date>",
        "",
        "  --dateList <date_list...>",
        "",
        "  --verbosity <verbosity>",
        "",
        "  --aRequiredInt <A_REQUIRED_INT>",
        "",
        "  --stringArray <string_array>",
        "",
        "  --integerList <integer_list>",
        "",
        "  --enumSet <enum_set>",
        "",
        "  --optionalInts <optional_ints...>",
        "",
        "  --listWrapper <list_wrapper>",
        "",
        "  --notFlag <NOT_FLAG>",
        "",
        "  --help",
        "    Print this help page.",
        "    The help flag may only be passed as the first argument.",
        "    Any further arguments will be ignored.",
        "",
        "");
  }
}
