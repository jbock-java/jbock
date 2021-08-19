package net.jbock.util;

import io.jbock.util.Either;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static io.jbock.util.Either.right;

final class ParseRequestSimple extends ParseRequest {

    private final List<String> args;

    ParseRequestSimple(List<String> args) {
        this.args = args;
    }

    @Override
    public Optional<Path> path() {
        return Optional.empty();
    }

    @Override
    public List<String> args() {
        return args;
    }

    @Override
    public Either<? extends AtFileError, List<String>> expand() {
        return right(args);
    }
}
