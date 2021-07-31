package net.jbock.convert.matching;

import io.jbock.util.Either;
import net.jbock.Option;
import net.jbock.Parameters;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.convert.matcher.ListMatcher;
import net.jbock.convert.matcher.Matcher;
import net.jbock.convert.matcher.OptionalMatcher;
import net.jbock.model.Multiplicity;
import net.jbock.source.SourceMethod;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Optional;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;
import static net.jbock.common.TypeTool.AS_PRIMITIVE;

@ValidateScope
public class MatchFinder {

    private final OptionalMatcher optionalMatcher;
    private final ListMatcher listMatcher;
    private final Types types;

    @Inject
    MatchFinder(
            OptionalMatcher optionalMatcher,
            ListMatcher listMatcher,
            Types types) {
        this.optionalMatcher = optionalMatcher;
        this.listMatcher = listMatcher;
        this.types = types;
    }

    public <M extends AnnotatedMethod> Either<String, Match<M>> findMatch(SourceMethod<M> parameter) {
        return validateMatch(parameter, findMatchInternal(parameter));
    }

    private <M extends AnnotatedMethod> Match<M> findMatchInternal(SourceMethod<M> parameter) {
        for (Matcher matcher : List.of(optionalMatcher, listMatcher)) {
            Optional<Match<M>> match = matcher.tryMatch(parameter);
            if (match.isPresent()) {
                return match.orElseThrow();
            }
        }
        TypeMirror baseType = AS_PRIMITIVE.visit(parameter.returnType())
                .map(types::boxedClass)
                .map(TypeElement::asType)
                .orElse(parameter.returnType());
        return Match.create(baseType, Multiplicity.REQUIRED, parameter);
    }

    private <M extends AnnotatedMethod> Either<String, Match<M>> validateMatch(
            SourceMethod<?> sourceMethod,
            Match<M> m) {
        if (sourceMethod.annotatedMethod().isParameter()
                && m.multiplicity() == Multiplicity.REPEATABLE) {
            return left("method '" +
                    sourceMethod.annotatedMethod().method().getSimpleName() +
                    "' returns a list-based type, so it must be annotated with @" +
                    Option.class.getSimpleName() +
                    " or @" +
                    Parameters.class.getSimpleName());
        }
        if (sourceMethod.annotatedMethod().isParameters()
                && m.multiplicity() != Multiplicity.REPEATABLE) {
            return left("method '" +
                    sourceMethod.annotatedMethod().method().getSimpleName() +
                    "' is annotated with @" +
                    Parameters.class.getSimpleName() +
                    ", so it must return java.util.List");
        }
        return right(m);
    }
}
