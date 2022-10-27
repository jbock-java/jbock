package net.jbock.convert.match;

import net.jbock.annotated.Executable;

import java.util.Optional;

abstract class Matcher {

    abstract <M extends Executable>
    Optional<Match<M>> tryMatch(
            M parameter);
}
