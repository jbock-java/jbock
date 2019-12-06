package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Param;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Command
abstract class VariousArguments {

  @Option(value = "bigDecimal")
  abstract BigDecimal bigDecimal();

  @Option(value = "bigDecimalList")
  abstract List<BigDecimal> bigDecimalList();

  @Option(value = "bigDecimalOpt")
  abstract Optional<BigDecimal> bigDecimalOpt();

  @Param(value = 1)
  abstract Optional<BigDecimal> bigDecimalPos();

  @Option(value = "bigInteger")
  abstract BigInteger bigInteger();

  @Option(value = "bigIntegerList")
  abstract List<BigInteger> bigIntegerList();

  @Option(value = "bigIntegerOpt")
  abstract Optional<BigInteger> bigIntegerOpt();

  @Param(value = 2)
  abstract Optional<BigInteger> bigIntegerPos();

  @Option(value = "fileList")
  abstract List<File> fileList();

  @Option(value = "fileOpt")
  abstract Optional<File> fileOpt();

  @Param(value = 3)
  abstract Optional<File> filePos();

  @Option(value = "path")
  abstract Path path();

  @Option(value = "pathList")
  abstract List<Path> pathList();

  @Option(value = "pathOpt")
  abstract Optional<Path> pathOpt();

  @Param(value = 4)
  abstract Optional<Path> pathPos();

  @Option(value = "localDate")
  abstract LocalDate localDate();

  @Option(value = "localDateList")
  abstract List<LocalDate> localDateList();

  @Option(value = "localDateOpt")
  abstract Optional<LocalDate> localDateOpt();

  @Param(value = 5)
  abstract Optional<LocalDate> localDatePos();

  @Option(value = "uri")
  abstract URI uri();

  @Option(value = "uriList")
  abstract List<URI> uriList();

  @Option(value = "uriOpt")
  abstract Optional<URI> uriOpt();

  @Param(value = 8)
  abstract Optional<URI> uriPos();

  @Option(value = "pattern")
  abstract Pattern pattern();

  @Option(value = "patternList")
  abstract List<Pattern> patternList();

  @Option(value = "patternOpt")
  abstract Optional<Pattern> patternOpt();

  @Param(value = 10)
  abstract Optional<Pattern> patternPos();
}
