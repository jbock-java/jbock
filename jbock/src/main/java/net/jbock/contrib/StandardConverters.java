package net.jbock.contrib;

import net.jbock.util.Either;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * This class converts from {@code String} to certain &quot;known types&quot;.
 */
public final class StandardConverters {

    private static String printX(Exception e) {
        return "caught " + e.getClass().getCanonicalName() + ", message: " + e.getMessage();
    }

    public static Either<String, Integer> asInteger(String token) {
        return Either.catching(Integer::valueOf, token).mapLeft(StandardConverters::printX);
    }

    public static Either<String, Long> asLong(String token) {
        return Either.catching(Long::valueOf, token).mapLeft(StandardConverters::printX);
    }

    public static Either<String, Short> asShort(String token) {
        return Either.catching(Short::valueOf, token).mapLeft(StandardConverters::printX);
    }

    public static Either<String, Byte> asByte(String token) {
        return Either.catching(Byte::valueOf, token).mapLeft(StandardConverters::printX);
    }

    public static Either<String, Float> asFloat(String token) {
        return Either.catching(Float::valueOf, token).mapLeft(StandardConverters::printX);
    }

    public static Either<String, Double> asDouble(String token) {
        return Either.catching(Double::valueOf, token).mapLeft(StandardConverters::printX);
    }

    public static Either<String, Character> asCharacter(String token) {
        return MoreConverters.asCharacter(token);
    }

    public static Either<String, Path> asPath(String token) {
        return Either.catching(Paths::get, token).mapLeft(StandardConverters::printX);
    }

    public static Either<String, URI> asURI(String token) {
        return Either.catching(URI::create, token).mapLeft(StandardConverters::printX);
    }

    public static Either<String, Pattern> asPattern(String token) {
        return Either.catching(Pattern::compile, token).mapLeft(StandardConverters::printX);
    }

    public static Either<String, LocalDate> asLocalDate(String token) {
        return Either.catching(LocalDate::parse, token).mapLeft(StandardConverters::printX);
    }

    public static Either<String, BigInteger> asBigInteger(String token) {
        return Either.catching(BigInteger::new, token).mapLeft(StandardConverters::printX);
    }

    public static Either<String, BigDecimal> asBigDecimal(String token) {
        return Either.catching(BigDecimal::new, token).mapLeft(StandardConverters::printX);
    }

    public static Either<String, File> asExistingFile(String token) {
        return MoreConverters.existingFile(token);
    }

    private StandardConverters() {
    }
}
