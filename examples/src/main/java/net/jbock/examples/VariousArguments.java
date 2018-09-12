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

  @Parameter
  abstract List<BigDecimal> bigDecimalList();

  @Parameter
  abstract Optional<BigDecimal> bigDecimalOpt();

  @PositionalParameter
  abstract Optional<BigDecimal> bigDecimalPos();

  @Parameter
  abstract BigInteger bigInteger();

  @Parameter
  abstract List<BigInteger> bigIntegerList();

  @Parameter
  abstract Optional<BigInteger> bigIntegerOpt();

  @PositionalParameter
  abstract Optional<BigInteger> bigIntegerPos();

  @Parameter
  abstract File file();

  @Parameter
  abstract List<File> fileList();

  @Parameter
  abstract Optional<File> fileOpt();

  @PositionalParameter
  abstract Optional<File> filePos();

  @Parameter
  abstract Path path();

  @Parameter
  abstract List<Path> pathList();

  @Parameter
  abstract Optional<Path> pathOpt();

  @PositionalParameter
  abstract Optional<Path> pathPos();

  @Parameter
  abstract LocalDate localDate();

  @Parameter
  abstract List<LocalDate> localDateList();

  @Parameter
  abstract Optional<LocalDate> localDateOpt();

  @PositionalParameter
  abstract Optional<LocalDate> localDatePos();

  @Parameter
  abstract LocalDateTime localDateTime();

  @Parameter
  abstract List<LocalDateTime> localDateTimeList();

  @Parameter
  abstract Optional<LocalDateTime> localDateTimeOpt();

  @PositionalParameter
  abstract Optional<LocalDateTime> localDateTimePos();

  @Parameter
  abstract OffsetDateTime offsetDateTime();

  @Parameter
  abstract List<OffsetDateTime> offsetDateTimeList();

  @Parameter
  abstract Optional<OffsetDateTime> offsetDateTimeOpt();

  @PositionalParameter
  abstract Optional<OffsetDateTime> offsetDateTimePos();

  @Parameter
  abstract ZonedDateTime zonedDateTime();

  @Parameter
  abstract List<ZonedDateTime> zonedDateTimeList();

  @Parameter
  abstract Optional<ZonedDateTime> zonedDateTimeOpt();

  @PositionalParameter
  abstract Optional<ZonedDateTime> zonedDateTimePos();

  @Parameter
  abstract URI uri();

  @Parameter
  abstract List<URI> uriList();

  @Parameter
  abstract Optional<URI> uriOpt();

  @PositionalParameter
  abstract Optional<URI> uriPos();

  @Parameter
  abstract Charset charset();

  @Parameter
  abstract List<Charset> charsetList();

  @Parameter
  abstract Optional<Charset> charsetOpt();

  @PositionalParameter
  abstract Optional<Charset> charsetPos();

  @Parameter
  abstract Pattern pattern();

  @Parameter
  abstract List<Pattern> patternList();

  @Parameter
  abstract Optional<Pattern> patternOpt();

  @PositionalParameter
  abstract Optional<Pattern> patternPos();

  @Parameter
  abstract Instant instant();

  @Parameter
  abstract List<Instant> instantList();

  @Parameter
  abstract Optional<Instant> instantOpt();

  @PositionalParameter
  abstract Optional<Instant> instantPos();
}
