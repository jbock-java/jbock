package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Positional;

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

  abstract BigDecimal bigDecimal();

  abstract List<BigDecimal> bigDecimalList();

  abstract Optional<BigDecimal> bigDecimalOpt();

  @Positional
  abstract Optional<BigDecimal> bigDecimalPos();

  abstract BigInteger bigInteger();

  abstract List<BigInteger> bigIntegerList();

  abstract Optional<BigInteger> bigIntegerOpt();

  @Positional
  abstract Optional<BigInteger> bigIntegerPos();

  abstract File file();

  abstract List<File> fileList();

  abstract Optional<File> fileOpt();

  @Positional
  abstract Optional<File> filePos();

  abstract Path path();

  abstract List<Path> pathList();

  abstract Optional<Path> pathOpt();

  @Positional
  abstract Optional<Path> pathPos();

  abstract LocalDate localDate();

  abstract List<LocalDate> localDateList();

  abstract Optional<LocalDate> localDateOpt();

  @Positional
  abstract Optional<LocalDate> localDatePos();

  abstract LocalDateTime localDateTime();

  abstract List<LocalDateTime> localDateTimeList();

  abstract Optional<LocalDateTime> localDateTimeOpt();

  @Positional
  abstract Optional<LocalDateTime> localDateTimePos();

  abstract OffsetDateTime offsetDateTime();

  abstract List<OffsetDateTime> offsetDateTimeList();

  abstract Optional<OffsetDateTime> offsetDateTimeOpt();

  @Positional
  abstract Optional<OffsetDateTime> offsetDateTimePos();

  abstract ZonedDateTime zonedDateTime();

  abstract List<ZonedDateTime> zonedDateTimeList();

  abstract Optional<ZonedDateTime> zonedDateTimeOpt();

  @Positional
  abstract Optional<ZonedDateTime> zonedDateTimePos();

  abstract URI uri();

  abstract List<URI> uriList();

  abstract Optional<URI> uriOpt();

  @Positional
  abstract Optional<URI> uriPos();

  abstract Charset charset();

  abstract List<Charset> charsetList();

  abstract Optional<Charset> charsetOpt();

  @Positional
  abstract Optional<Charset> charsetPos();

  abstract Pattern pattern();

  abstract List<Pattern> patternList();

  abstract Optional<Pattern> patternOpt();

  @Positional
  abstract Optional<Pattern> patternPos();

  abstract Instant instant();

  abstract List<Instant> instantList();

  abstract Optional<Instant> instantOpt();

  @Positional
  abstract Optional<Instant> instantPos();
}
