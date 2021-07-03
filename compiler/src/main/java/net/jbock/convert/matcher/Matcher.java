package net.jbock.convert.matcher;

import io.jbock.util.Optional;
import net.jbock.convert.matching.Match;
import net.jbock.parameter.AbstractItem;

public interface Matcher {

    Optional<Match> tryMatch(AbstractItem parameter);
}
