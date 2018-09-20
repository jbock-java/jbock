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
        "--anInt", "50",
        "--notFlag", "true",
        "--integerList", "1,2,3,4",
        "--enumSet", "FOO",
        "true", "false", "true",
        "--stringArray", "A",
        "--anOptionalInt", "51");
    assertEquals(1500000000000L, parsed.date().getTime());
    assertEquals(Optional.of(1500000000000L), parsed.optDate().map(Date::getTime));
    assertEquals(1500000000000L, parsed.dateList().get(0).getTime());
    assertEquals(Optional.of(16), parsed.verbosity().map(BigInteger::intValue));
    assertEquals(50, parsed.anInt());
    assertEquals(Arrays.asList(true, false, true), parsed.booleanList());
    assertEquals(OptionalInt.of(51), parsed.anOptionalInt());
    assertEquals(Arrays.asList(1, 2, 3, 4), parsed.integerList().orElseThrow(AssertionFailedError::new));
    assertEquals(singleton(MyEnum.FOO), parsed.enumSet().orElseThrow(AssertionFailedError::new));
    assertArrayEquals(new String[]{"A"}, parsed.stringArray().orElseThrow(AssertionFailedError::new));
    assertTrue(parsed.notFlag());
  }

  @Test
  void invalidOptions() {
    f.assertThat("--date", "FooBar").failsWithLine4("For input string: \"FooBar\"");
    f.assertThat().failsWithLine4("Missing required option: DATE (--date)");
  }

  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  CustomMapperArguments",
        "",
        "SYNOPSIS",
        "  CustomMapperArguments [<options>] --date=<DATE> --anInt=<AN_INT> --notFlag=<NOT_FLAG> [[--] <boolean_list...>]",
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
        "  --anInt <AN_INT>",
        "",
        "  --anOptionalInt <an_optional_int>",
        "",
        "  --stringArray <string_array>",
        "",
        "  --integerList <integer_list>",
        "",
        "  --enumSet <enum_set>",
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
