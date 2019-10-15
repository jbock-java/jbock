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

  @Parameter(longName = "bigDecimalList")
  abstract List<BigDecimal> bigDecimalList();

  @Parameter(longName = "bigDecimalOpt")
  abstract Optional<BigDecimal> bigDecimalOpt();

  @PositionalParameter
  abstract Optional<BigDecimal> bigDecimalPos();

  @Parameter(longName = "bigInteger")
  abstract BigInteger bigInteger();

  @Parameter(longName = "bigIntegerList")
  abstract List<BigInteger> bigIntegerList();

  @Parameter(longName = "bigIntegerOpt")
  abstract Optional<BigInteger> bigIntegerOpt();

  @PositionalParameter(position = 1)
  abstract Optional<BigInteger> bigIntegerPos();

  @Parameter(longName = "file")
  abstract File file();

  @Parameter(longName = "fileList")
  abstract List<File> fileList();

  @Parameter(longName = "fileOpt")
  abstract Optional<File> fileOpt();

  @PositionalParameter(position = 2)
  abstract Optional<File> filePos();

  @Parameter(longName = "path")
  abstract Path path();

  @Parameter(longName = "pathList")
  abstract List<Path> pathList();

  @Parameter(longName = "pathOpt")
  abstract Optional<Path> pathOpt();

  @PositionalParameter(position = 3)
  abstract Optional<Path> pathPos();

  @Parameter(longName = "localDate")
  abstract LocalDate localDate();

  @Parameter(longName = "localDateList")
  abstract List<LocalDate> localDateList();

  @Parameter(longName = "localDateOpt")
  abstract Optional<LocalDate> localDateOpt();

  @PositionalParameter(position = 4)
  abstract Optional<LocalDate> localDatePos();

  @Parameter(longName = "uri")
  abstract URI uri();

  @Parameter(longName = "uriList")
  abstract List<URI> uriList();

  @Parameter(longName = "uriOpt")
  abstract Optional<URI> uriOpt();

  @PositionalParameter(position = 8)
  abstract Optional<URI> uriPos();

  @Parameter(longName = "pattern")
  abstract Pattern pattern();

  @Parameter(longName = "patternList")
  abstract List<Pattern> patternList();

  @Parameter(longName = "patternOpt")
  abstract Optional<Pattern> patternOpt();

  @PositionalParameter(position = 10)
  abstract Optional<Pattern> patternPos();
}
