package net.jbock.convert.matching;

import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.model.Multiplicity;
import net.jbock.source.SourceMethod;

import java.util.Optional;

abstract class MatchValidator {

    /* Left-Optional
     */
    Optional<String> validateMatch(SourceMethod<?> sourceMethod, Match m) {
        if (sourceMethod.isParameter()
                && m.multiplicity() == Multiplicity.REPEATABLE) {
            return Optional.of("use @" + Parameters.class.getSimpleName() + " here");
        }
        if (sourceMethod.isParameters()
                && m.multiplicity() != Multiplicity.REPEATABLE) {
            return Optional.of("use @" + Parameter.class.getSimpleName() + " here");
        }
        return Optional.empty();
    }
}
