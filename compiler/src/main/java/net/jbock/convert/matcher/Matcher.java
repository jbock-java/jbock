package net.jbock.convert.matcher;

import net.jbock.convert.matching.Match;
import net.jbock.source.SourceMethod;

import java.util.Optional;

public interface Matcher {

    Optional<Match> tryMatch(SourceMethod<?> parameter);
}
