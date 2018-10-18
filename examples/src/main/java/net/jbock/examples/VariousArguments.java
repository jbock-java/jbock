package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@CommandLineArguments
abstract class VariousArguments {

  @Parameter(longName = "bigDecimal")
  abstract BigDecimal bigDecimal();

  @Parameter(longName = "bigDecimalList", repeatable = true)
  abstract List<BigDecimal> bigDecimalList();

  @Parameter(longName = "bigDecimalOpt", optional = true)
  abstract Optional<BigDecimal> bigDecimalOpt();

  @PositionalParameter(optional = true)
  abstract Optional<BigDecimal> bigDecimalPos();

  @Parameter(longName = "bigInteger")
  abstract BigInteger bigInteger();

  @Parameter(longName = "bigIntegerList", repeatable = true)
  abstract List<BigInteger> bigIntegerList();

  @Parameter(longName = "bigIntegerOpt", optional = true)
  abstract Optional<BigInteger> bigIntegerOpt();

  @PositionalParameter(optional = true, position = 1)
  abstract Optional<BigInteger> bigIntegerPos();

  @Parameter(longName = "file")
  abstract File file();

  @Parameter(longName = "fileList", repeatable = true)
  abstract List<File> fileList();

  @Parameter(longName = "fileOpt", optional = true)
  abstract Optional<File> fileOpt();

  @PositionalParameter(optional = true, position = 2)
  abstract Optional<File> filePos();

  @Parameter(longName = "path")
  abstract Path path();

  @Parameter(longName = "pathList", repeatable = true)
  abstract List<Path> pathList();

  @Parameter(longName = "pathOpt", optional = true)
  abstract Optional<Path> pathOpt();

  @PositionalParameter(optional = true, position = 3)
  abstract Optional<Path> pathPos();

  @Parameter(longName = "localDate")
  abstract LocalDate localDate();

  @Parameter(longName = "localDateList", repeatable = true)
  abstract List<LocalDate> localDateList();

  @Parameter(longName = "localDateOpt", optional = true)
  abstract Optional<LocalDate> localDateOpt();

  @PositionalParameter(optional = true, position = 4)
  abstract Optional<LocalDate> localDatePos();

  @Parameter(longName = "localDateTime")
  abstract LocalDateTime localDateTime();

  @Parameter(longName = "localDateTimeList", repeatable = true)
  abstract List<LocalDateTime> localDateTimeList();

  @Parameter(longName = "localDateTimeOpt", optional = true)
  abstract Optional<LocalDateTime> localDateTimeOpt();

  @PositionalParameter(optional = true, position = 5)
  abstract Optional<LocalDateTime> localDateTimePos();

  @Parameter(longName = "offsetDateTime")
  abstract OffsetDateTime offsetDateTime();

  @Parameter(longName = "offsetDateTimeList", repeatable = true)
  abstract List<OffsetDateTime> offsetDateTimeList();

  @Parameter(longName = "offsetDateTimeOpt", optional = true)
  abstract Optional<OffsetDateTime> offsetDateTimeOpt();

  @PositionalParameter(optional = true, position = 6)
  abstract Optional<OffsetDateTime> offsetDateTimePos();

  @Parameter(longName = "zonedDateTime")
  abstract ZonedDateTime zonedDateTime();

  @Parameter(longName = "zonedDateTimeList", repeatable = true)
  abstract List<ZonedDateTime> zonedDateTimeList();

  @Parameter(longName = "zonedDateTimeOpt", optional = true)
  abstract Optional<ZonedDateTime> zonedDateTimeOpt();

  @PositionalParameter(optional = true, position = 7)
  abstract Optional<ZonedDateTime> zonedDateTimePos();

  @Parameter(longName = "uri")
  abstract URI uri();

  @Parameter(longName = "uriList", repeatable = true)
  abstract List<URI> uriList();

  @Parameter(longName = "uriOpt", optional = true)
  abstract Optional<URI> uriOpt();

  @PositionalParameter(optional = true, position = 8)
  abstract Optional<URI> uriPos();

  @Parameter(longName = "charset")
  abstract Charset charset();

  @Parameter(longName = "charsetList", repeatable = true)
  abstract List<Charset> charsetList();

  @Parameter(longName = "charsetOpt", optional = true)
  abstract Optional<Charset> charsetOpt();

  @PositionalParameter(optional = true, position = 9)
  abstract Optional<Charset> charsetPos();

  @Parameter(longName = "pattern")
  abstract Pattern pattern();

  @Parameter(longName = "patternList", repeatable = true)
  abstract List<Pattern> patternList();

  @Parameter(longName = "patternOpt", optional = true)
  abstract Optional<Pattern> patternOpt();

  @PositionalParameter(optional = true, position = 10)
  abstract Optional<Pattern> patternPos();

  @Parameter(longName = "instant")
  abstract Instant instant();

  @Parameter(longName = "instantList", repeatable = true)
  abstract List<Instant> instantList();

  @Parameter(longName = "instantOpt", optional = true)
  abstract Optional<Instant> instantOpt();

  @PositionalParameter(optional = true, position = 11)
  abstract Optional<Instant> instantPos();
}
