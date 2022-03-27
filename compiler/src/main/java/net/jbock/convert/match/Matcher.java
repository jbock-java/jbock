package net.jbock.convert.match;

import net.jbock.annotated.AnnotatedMethod;

import java.util.Optional;

interface Matcher {

    <M extends AnnotatedMethod>
    Optional<Match<M>> tryMatch(
            M parameter);
}
