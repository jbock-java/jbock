package net.jbock.state;

import net.jbock.util.ExFailure;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface Parser<T> {

    void parse(List<String> tokens) throws ExFailure;

    Stream<String> rest();

    Stream<String> option(T option);

    Optional<String> param(int index);
}
