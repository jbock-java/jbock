package net.jbock.convert.matching;

import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.model.Multiplicity;
import net.jbock.parameter.SourceMethod;

import java.util.Optional;

abstract class MatchValidator {

    private final SourceMethod sourceMethod;

    MatchValidator(SourceMethod sourceMethod) {
        this.sourceMethod = sourceMethod;
    }

    /* Left-Optional
     */
    Optional<String> validateMatch(Match m) {
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
