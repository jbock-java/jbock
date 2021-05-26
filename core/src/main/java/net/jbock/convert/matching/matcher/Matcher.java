package net.jbock.convert.matching.matcher;

import net.jbock.convert.matching.Match;
import net.jbock.parameter.AbstractParameter;

import java.util.Optional;

public abstract class Matcher {

  public abstract Optional<Match> tryMatch(AbstractParameter parameter);
}
