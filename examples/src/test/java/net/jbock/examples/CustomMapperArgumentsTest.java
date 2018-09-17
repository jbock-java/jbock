package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomMapperArgumentsTest {

  private ParserTestFixture<CustomMapperArguments> f =
      ParserTestFixture.create(CustomMapperArguments_Parser.create());

  @Test
  void success() {
    CustomMapperArguments parsed = f.parse(
        "--date", "1500000000000",
        "--optDate", "1500000000000",
        "--dateList", "1500000000000",
        "--verbosity", "0x10");
    assertEquals(1500000000000L, parsed.date().getTime());
    assertEquals(Optional.of(1500000000000L), parsed.optDate().map(Date::getTime));
    assertEquals(1500000000000L, parsed.dateList().get(0).getTime());
    assertEquals(Optional.of(16), parsed.verbosity().map(BigInteger::intValue));
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
        "  CustomMapperArguments [<options>] --date=<DATE>",
        "",
        "DESCRIPTION",
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
        "  --help",
        "    Print this help page.",
        "    The help flag may only be passed as the first argument.",
        "    Any further arguments will be ignored.",
        "",
        "");
  }
}
