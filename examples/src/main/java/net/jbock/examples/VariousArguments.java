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
abstract class VariousArguments {

    @Option(names = "--bigDecimal")
    abstract BigDecimal bigDecimal();

    @Option(names = "--bigDecimalList")
    abstract List<BigDecimal> bigDecimalList();

    @Option(names = "--bigDecimalOpt")
    abstract Optional<BigDecimal> bigDecimalOpt();

    @Parameter(index = 0)
    abstract Optional<BigDecimal> bigDecimalPos();

    @Option(names = "--bigInteger")
    abstract BigInteger bigInteger();

    @Option(names = "--bigIntegerList")
    abstract List<BigInteger> bigIntegerList();

    @Option(names = "--bigIntegerOpt")
    abstract Optional<BigInteger> bigIntegerOpt();

    @Option(names = "--vavrOpt")
    abstract io.vavr.control.Option<Integer> vavrOpt();

    @Parameter(index = 1)
    abstract Optional<BigInteger> bigIntegerPos();

    @Option(names = "--fileList")
    abstract List<File> fileList();

    @Option(names = "--fileOpt")
    abstract Optional<File> fileOpt();

    @Parameter(index = 2)
    abstract Optional<File> filePos();

    @Option(names = "--path")
    abstract Path path();

    @Option(names = "--pathList")
    abstract List<Path> pathList();

    @Option(names = "--pathOpt")
    abstract Optional<Path> pathOpt();

    @Parameter(index = 3)
    abstract Optional<Path> pathPos();

    @Option(names = "--localDate")
    abstract LocalDate localDate();

    @Option(names = "--localDateList")
    abstract List<LocalDate> localDateList();

    @Option(names = "--localDateOpt")
    abstract Optional<LocalDate> localDateOpt();

    @Parameter(index = 4)
    abstract Optional<LocalDate> localDatePos();

    @Option(names = "--uri")
    abstract URI uri();

    @Option(names = "--uriList")
    abstract List<URI> uriList();

    @Option(names = "--uriOpt")
    abstract Optional<URI> uriOpt();

    @Parameter(index = 5)
    abstract Optional<URI> uriPos();

    @Option(names = "--pattern")
    abstract Pattern pattern();

    @Option(names = "--patternList")
    abstract List<Pattern> patternList();

    @Option(names = "--patternOpt")
    abstract Optional<Pattern> patternOpt();

    @Parameter(index = 6)
    abstract Optional<Pattern> patternPos();
}
