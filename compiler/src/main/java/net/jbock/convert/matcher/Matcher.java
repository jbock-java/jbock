package net.jbock.convert.matcher;

import net.jbock.convert.matching.Match;
import net.jbock.either.Optional;
import net.jbock.parameter.AbstractItem;

public interface Matcher {

  Optional<Match> tryMatch(AbstractItem parameter);
}
