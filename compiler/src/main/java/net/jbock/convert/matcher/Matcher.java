package net.jbock.convert.matcher;

import net.jbock.annotated.AnnotatedMethod;
import net.jbock.convert.matching.Match;

import java.util.Optional;

public interface Matcher {

    <M extends AnnotatedMethod>
    Optional<Match<M>> tryMatch(
            M parameter);
}
