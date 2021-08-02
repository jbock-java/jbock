package net.jbock.convert.matching;

import io.jbock.util.Either;
import net.jbock.Option;
import net.jbock.Parameters;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.matcher.ListMatcher;
import net.jbock.convert.matcher.OptionalMatcher;
import net.jbock.model.Multiplicity;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Optional;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;
import static javax.lang.model.type.TypeKind.BOOLEAN;
import static net.jbock.common.TypeTool.AS_PRIMITIVE;
import static net.jbock.model.Multiplicity.OPTIONAL;

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

    public <M extends AnnotatedMethod> Either<ValidationFailure, ValidMatch<M>> findMatch(
            M sourceMethod) {
        return validateMatch(findMatchInternal(sourceMethod));
    }

    public Either<ValidationFailure, ValidMatch<AnnotatedOption>> findFlagMatch(
            AnnotatedOption sourceMethod) {
        PrimitiveType bool = types.getPrimitiveType(BOOLEAN);
        Match<AnnotatedOption> match = Match.create(bool, OPTIONAL, sourceMethod);
        return validateMatch(match);
    }

    private <M extends AnnotatedMethod> Match<M> findMatchInternal(
            M sourceMethod) {
        return List.of(optionalMatcher, listMatcher).stream()
                .map(matcher -> matcher.tryMatch(sourceMethod))
                .flatMap(Optional::stream)
                .findFirst()
                .orElseGet(() -> {
                    TypeMirror baseType = AS_PRIMITIVE.visit(sourceMethod.returnType())
                            .map(types::boxedClass)
                            .map(TypeElement::asType)
                            .orElse(sourceMethod.returnType());
                    return Match.create(baseType, Multiplicity.REQUIRED, sourceMethod);
                });
    }

    private static <M extends AnnotatedMethod> Either<ValidationFailure, ValidMatch<M>> validateMatch(
            Match<M> m) {
        M sourceMethod = m.sourceMethod();
        if (sourceMethod.isParameter()
                && m.multiplicity() == Multiplicity.REPEATABLE) {
            return left(sourceMethod.fail("method '" +
                    sourceMethod.method().getSimpleName() +
                    "' returns a list-based type, so it must be annotated with @" +
                    Option.class.getSimpleName() +
                    " or @" +
                    Parameters.class.getSimpleName()));
        }
        if (sourceMethod.isParameters()
                && m.multiplicity() != Multiplicity.REPEATABLE) {
            return left(sourceMethod.fail("method '" +
                    sourceMethod.method().getSimpleName() +
                    "' is annotated with @" +
                    Parameters.class.getSimpleName() +
                    ", so it must return java.util.List"));
        }
        return right(new ValidMatch<>(m));
    }
}
