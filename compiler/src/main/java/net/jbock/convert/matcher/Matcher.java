package net.jbock.convert.matcher;

import net.jbock.convert.matching.Match;
import net.jbock.parameter.AbstractParameter;

import java.util.Optional;

public interface Matcher {

  Optional<Match> tryMatch(AbstractParameter parameter);
}
