package net.jbock.examples;

import net.jbock.CommandLineArguments;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class VariousArguments {

  abstract BigDecimal bigDecimal();

  abstract List<BigDecimal> bigDecimalList();

  abstract Optional<BigDecimal> bigDecimalOpt();

  abstract BigInteger bigInteger();

  abstract List<BigInteger> bigIntegerList();

  abstract Optional<BigInteger> bigIntegerOpt();

  abstract File file();

  abstract List<File> fileList();

  abstract Optional<File> fileOpt();

  abstract Path path();

  abstract List<Path> pathList();

  abstract Optional<Path> pathOpt();

  abstract LocalDate localDate();

  abstract List<LocalDate> localDateList();

  abstract Optional<LocalDate> localDateOpt();

  abstract LocalDateTime localDateTime();

  abstract List<LocalDateTime> localDateTimeList();

  abstract Optional<LocalDateTime> localDateTimeOpt();

  abstract OffsetDateTime offsetDateTime();

  abstract List<OffsetDateTime> offsetDateTimeList();

  abstract Optional<OffsetDateTime> offsetDateTimeOpt();

  abstract ZonedDateTime zonedDateTime();

  abstract List<ZonedDateTime> zonedDateTimeList();

  abstract Optional<ZonedDateTime> zonedDateTimeOpt();

  abstract URI uri();

  abstract List<URI> uriList();

  abstract Optional<URI> uriOpt();
}
