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

  @Parameter
  abstract BigDecimal bigDecimal();

  @Parameter(repeatable = true)
  abstract List<BigDecimal> bigDecimalList();

  @Parameter(optional = true)
  abstract Optional<BigDecimal> bigDecimalOpt();

  @PositionalParameter(optional = true)
  abstract Optional<BigDecimal> bigDecimalPos();

  @Parameter
  abstract BigInteger bigInteger();

  @Parameter(repeatable = true)
  abstract List<BigInteger> bigIntegerList();

  @Parameter(optional = true)
  abstract Optional<BigInteger> bigIntegerOpt();

  @PositionalParameter(optional = true)
  abstract Optional<BigInteger> bigIntegerPos();

  @Parameter
  abstract File file();

  @Parameter(repeatable = true)
  abstract List<File> fileList();

  @Parameter(optional = true)
  abstract Optional<File> fileOpt();

  @PositionalParameter(optional = true)
  abstract Optional<File> filePos();

  @Parameter
  abstract Path path();

  @Parameter(repeatable = true)
  abstract List<Path> pathList();

  @Parameter(optional = true)
  abstract Optional<Path> pathOpt();

  @PositionalParameter(optional = true)
  abstract Optional<Path> pathPos();

  @Parameter
  abstract LocalDate localDate();

  @Parameter(repeatable = true)
  abstract List<LocalDate> localDateList();

  @Parameter(optional = true)
  abstract Optional<LocalDate> localDateOpt();

  @PositionalParameter(optional = true)
  abstract Optional<LocalDate> localDatePos();

  @Parameter
  abstract LocalDateTime localDateTime();

  @Parameter(repeatable = true)
  abstract List<LocalDateTime> localDateTimeList();

  @Parameter(optional = true)
  abstract Optional<LocalDateTime> localDateTimeOpt();

  @PositionalParameter(optional = true)
  abstract Optional<LocalDateTime> localDateTimePos();

  @Parameter
  abstract OffsetDateTime offsetDateTime();

  @Parameter(repeatable = true)
  abstract List<OffsetDateTime> offsetDateTimeList();

  @Parameter(optional = true)
  abstract Optional<OffsetDateTime> offsetDateTimeOpt();

  @PositionalParameter(optional = true)
  abstract Optional<OffsetDateTime> offsetDateTimePos();

  @Parameter
  abstract ZonedDateTime zonedDateTime();

  @Parameter(repeatable = true)
  abstract List<ZonedDateTime> zonedDateTimeList();

  @Parameter(optional = true)
  abstract Optional<ZonedDateTime> zonedDateTimeOpt();

  @PositionalParameter(optional = true)
  abstract Optional<ZonedDateTime> zonedDateTimePos();

  @Parameter
  abstract URI uri();

  @Parameter(repeatable = true)
  abstract List<URI> uriList();

  @Parameter(optional = true)
  abstract Optional<URI> uriOpt();

  @PositionalParameter(optional = true)
  abstract Optional<URI> uriPos();

  @Parameter
  abstract Charset charset();

  @Parameter(repeatable = true)
  abstract List<Charset> charsetList();

  @Parameter(optional = true)
  abstract Optional<Charset> charsetOpt();

  @PositionalParameter(optional = true)
  abstract Optional<Charset> charsetPos();

  @Parameter
  abstract Pattern pattern();

  @Parameter(repeatable = true)
  abstract List<Pattern> patternList();

  @Parameter(optional = true)
  abstract Optional<Pattern> patternOpt();

  @PositionalParameter(optional = true)
  abstract Optional<Pattern> patternPos();

  @Parameter
  abstract Instant instant();

  @Parameter(repeatable = true)
  abstract List<Instant> instantList();

  @Parameter(optional = true)
  abstract Optional<Instant> instantOpt();

  @PositionalParameter(optional = true)
  abstract Optional<Instant> instantPos();
}
