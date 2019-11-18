package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@CommandLineArguments
abstract class VariousArguments {

  @Parameter(value = "bigDecimal")
  abstract BigDecimal bigDecimal();

  @Parameter(value = "bigDecimalList")
  abstract List<BigDecimal> bigDecimalList();

  @Parameter(value = "bigDecimalOpt")
  abstract Optional<BigDecimal> bigDecimalOpt();

  @PositionalParameter(value = 1)
  abstract Optional<BigDecimal> bigDecimalPos();

  @Parameter(value = "bigInteger")
  abstract BigInteger bigInteger();

  @Parameter(value = "bigIntegerList")
  abstract List<BigInteger> bigIntegerList();

  @Parameter(value = "bigIntegerOpt")
  abstract Optional<BigInteger> bigIntegerOpt();

  @PositionalParameter(value = 2)
  abstract Optional<BigInteger> bigIntegerPos();

  @Parameter(value = "fileList")
  abstract List<File> fileList();

  @Parameter(value = "fileOpt")
  abstract Optional<File> fileOpt();

  @PositionalParameter(value = 3)
  abstract Optional<File> filePos();

  @Parameter(value = "path")
  abstract Path path();

  @Parameter(value = "pathList")
  abstract List<Path> pathList();

  @Parameter(value = "pathOpt")
  abstract Optional<Path> pathOpt();

  @PositionalParameter(value = 4)
  abstract Optional<Path> pathPos();

  @Parameter(value = "localDate")
  abstract LocalDate localDate();

  @Parameter(value = "localDateList")
  abstract List<LocalDate> localDateList();

  @Parameter(value = "localDateOpt")
  abstract Optional<LocalDate> localDateOpt();

  @PositionalParameter(value = 5)
  abstract Optional<LocalDate> localDatePos();

  @Parameter(value = "uri")
  abstract URI uri();

  @Parameter(value = "uriList")
  abstract List<URI> uriList();

  @Parameter(value = "uriOpt")
  abstract Optional<URI> uriOpt();

  @PositionalParameter(value = 8)
  abstract Optional<URI> uriPos();

  @Parameter(value = "pattern")
  abstract Pattern pattern();

  @Parameter(value = "patternList")
  abstract List<Pattern> patternList();

  @Parameter(value = "patternOpt")
  abstract Optional<Pattern> patternOpt();

  @PositionalParameter(value = 10)
  abstract Optional<Pattern> patternPos();
}
