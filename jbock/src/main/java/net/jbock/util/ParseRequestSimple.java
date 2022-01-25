package net.jbock.util;

import io.jbock.util.Either;

import java.util.List;

import static io.jbock.util.Either.right;

final class ParseRequestSimple extends ParseRequest {

    private final List<String> args;

    ParseRequestSimple(List<String> args) {
        this.args = args;
    }

    @Override
    public Either<? extends AtFileError, List<String>> expand() {
        return right(args);
    }
}
