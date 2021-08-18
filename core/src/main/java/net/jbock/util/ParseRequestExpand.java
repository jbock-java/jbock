package net.jbock.util;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

final class ParseRequestExpand extends ParseRequest {

    private final Path path;
    private final List<String> args;

    ParseRequestExpand(Path path, List<String> args) {
        this.path = path;
        this.args = args;
    }

    @Override
    public Optional<Path> path() {
        return Optional.of(path);
    }

    @Override
    public List<String> args() {
        return args;
    }
}
