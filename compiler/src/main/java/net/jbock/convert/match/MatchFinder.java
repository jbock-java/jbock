package net.jbock.convert.match;

import io.jbock.util.Either;
import jakarta.inject.Inject;
import net.jbock.VarargsParameter;
import net.jbock.annotated.Executable;
import net.jbock.common.SafeTypes;
import net.jbock.common.ValidationFailure;
import net.jbock.model.Multiplicity;
import net.jbock.validate.ValidateScope;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.Set;

import static io.jbock.util.Either.right;
import static javax.lang.model.type.TypeKind.BOOLEAN;
import static net.jbock.common.TypeTool.AS_PRIMITIVE;
import static net.jbock.model.Multiplicity.OPTIONAL;
import static net.jbock.model.Multiplicity.REQUIRED;

@ValidateScope
public class MatchFinder {

    private final Set<Matcher> matchers;
    private final SafeTypes types;

    @Inject
    MatchFinder(
            Set<Matcher> matchers,
            SafeTypes types) {
        this.matchers = matchers;
        this.types = types;
    }

    public <M extends Executable>
    Either<ValidationFailure, Match<M>> findMatch(
            M sourceMethod) {
        Match<M> match = findMatchInternal(sourceMethod);
        return validateVarargsIsList(match)
                .<Either<ValidationFailure, Match<M>>>map(Either::left)
                .orElseGet(() -> right(match));
    }

    public <M extends Executable>
    Either<ValidationFailure, Match<M>>
    createNullaryMatch(
            M sourceMethod) {
        PrimitiveType bool = types.getPrimitiveType(BOOLEAN);
        Match<M> match = Match.create(bool, OPTIONAL, sourceMethod);
        return validateVarargsIsList(match)
                .<Either<ValidationFailure, Match<M>>>map(Either::left)
                .orElseGet(() -> right(match));
    }

    private <M extends Executable> Match<M>
    findMatchInternal(
            M sourceMethod) {
        return matchers.stream()
                .map(matcher -> matcher.tryMatch(sourceMethod))
                .flatMap(Optional::stream)
                .findFirst()
                .orElseGet(() -> {
                    TypeMirror baseType = AS_PRIMITIVE.visit(sourceMethod.returnType())
                            .map(types::boxedClass)
                            .map(TypeElement::asType)
                            .orElse(sourceMethod.returnType());
                    return Match.create(baseType, REQUIRED, sourceMethod);
                });
    }

    private <M extends Executable>
    Optional<ValidationFailure> validateVarargsIsList(
            Match<M> match) {
        M sourceMethod = match.sourceMethod();
        if (sourceMethod.isVarargsParameter()
                && match.multiplicity() != Multiplicity.REPEATABLE) {
            return Optional.of(sourceMethod.fail("method '" +
                    sourceMethod.method().getSimpleName() +
                    "' is annotated with @" +
                    VarargsParameter.class.getSimpleName() +
                    ", so it must return java.util.List"));
        }
        return Optional.empty();
    }
}
