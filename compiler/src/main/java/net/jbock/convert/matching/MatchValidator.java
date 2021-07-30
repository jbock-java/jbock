package net.jbock.convert.matching;

import io.jbock.util.Either;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.model.Multiplicity;
import net.jbock.source.SourceMethod;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;

abstract class MatchValidator {

    <M extends AnnotatedMethod> Either<String, Match<M>> validateMatch(
            SourceMethod<?> sourceMethod,
            Match<M> m) {
        if (sourceMethod.annotatedMethod().isParameter()
                && m.multiplicity() == Multiplicity.REPEATABLE) {
            return left("use @" + Parameters.class.getSimpleName() + " here");
        }
        if (sourceMethod.annotatedMethod().isParameters()
                && m.multiplicity() != Multiplicity.REPEATABLE) {
            return left("use @" + Parameter.class.getSimpleName() + " here");
        }
        return right(m);
    }
}
