package net.jbock.convert.match;

import net.jbock.annotated.Item;

import java.util.Optional;

abstract class Matcher {

    abstract <M extends Item>
    Optional<Match<M>> tryMatch(
            M parameter);
}
