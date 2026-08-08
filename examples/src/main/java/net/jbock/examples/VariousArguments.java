package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;

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
interface VariousArguments {

    @Option(names = "--bigDecimal")
    BigDecimal bigDecimal();

    @Option(names = "--bigDecimalList")
    List<BigDecimal> bigDecimalList();

    @Option(names = "--bigDecimalOpt")
    Optional<BigDecimal> bigDecimalOpt();

    @Parameter(index = 0)
    Optional<BigDecimal> bigDecimalPos();

    @Option(names = "--bigInteger")
    BigInteger bigInteger();

    @Option(names = "--bigIntegerList")
    List<BigInteger> bigIntegerList();

    @Option(names = "--bigIntegerOpt")
    Optional<BigInteger> bigIntegerOpt();

    @Parameter(index = 1)
    Optional<BigInteger> bigIntegerPos();

    @Option(names = "--fileList")
    List<File> fileList();

    @Option(names = "--fileOpt")
    Optional<File> fileOpt();

    @Parameter(index = 2)
    Optional<File> filePos();

    @Option(names = "--path")
    Path path();

    @Option(names = "--pathList")
    List<Path> pathList();

    @Option(names = "--pathOpt")
    Optional<Path> pathOpt();

    @Parameter(index = 3)
    Optional<Path> pathPos();

    @Option(names = "--localDate")
    LocalDate localDate();

    @Option(names = "--localDateList")
    List<LocalDate> localDateList();

    @Option(names = "--localDateOpt")
    Optional<LocalDate> localDateOpt();

    @Parameter(index = 4)
    Optional<LocalDate> localDatePos();

    @Option(names = "--uri")
    URI uri();

    @Option(names = "--uriList")
    List<URI> uriList();

    @Option(names = "--uriOpt")
    Optional<URI> uriOpt();

    @Parameter(index = 5)
    Optional<URI> uriPos();

    @Option(names = "--pattern")
    Pattern pattern();

    @Option(names = "--patternList")
    List<Pattern> patternList();

    @Option(names = "--patternOpt")
    Optional<Pattern> patternOpt();

    @Parameter(index = 6)
    Optional<Pattern> patternPos();
}
