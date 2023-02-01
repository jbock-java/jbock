package net.jbock.convert.match;

import io.jbock.simple.Inject;
import io.jbock.util.Either;
import net.jbock.VarargsParameter;
import net.jbock.annotated.Item;
import net.jbock.common.SafeTypes;
import net.jbock.common.TypeTool;
import net.jbock.common.ValidationFailure;
import net.jbock.model.Multiplicity;

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

public final class MatchFinder {

    private final Set<Matcher> matchers;
    private final SafeTypes types;

    @Inject
    public MatchFinder(
            Set<Matcher> matchers,
            TypeTool tool) {
        this.matchers = matchers;
        this.types = tool.types();
    }

    public <M extends Item>
    Either<ValidationFailure, Match<M>> findMatch(M item) {
        Match<M> match = findMatchInternal(item);
        return validateVarargsIsList(match)
                .<Either<ValidationFailure, Match<M>>>map(Either::left)
                .orElseGet(() -> right(match));
    }

    public <M extends Item>
    Either<ValidationFailure, Match<M>>
    createNullaryMatch(M item) {
        PrimitiveType bool = types.getPrimitiveType(BOOLEAN);
        Match<M> match = Match.create(bool, OPTIONAL, item);
        return validateVarargsIsList(match)
                .<Either<ValidationFailure, Match<M>>>map(Either::left)
                .orElseGet(() -> right(match));
    }

    private <M extends Item> Match<M>
    findMatchInternal(M item) {
        return matchers.stream()
                .map(matcher -> matcher.tryMatch(item))
                .flatMap(Optional::stream)
                .findFirst()
                .orElseGet(() -> {
                    TypeMirror baseType = AS_PRIMITIVE.visit(item.returnType())
                            .map(types::boxedClass)
                            .map(TypeElement::asType)
                            .orElse(item.returnType());
                    return Match.create(baseType, REQUIRED, item);
                });
    }

    private <M extends Item>
    Optional<ValidationFailure> validateVarargsIsList(Match<M> match) {
        M item = match.item();
        if (item.isVarargsParameter()
                && match.multiplicity() != Multiplicity.REPEATABLE) {
            return Optional.of(item.fail("method '" +
                    item.method().getSimpleName() +
                    "' is annotated with @" +
                    VarargsParameter.class.getSimpleName() +
                    ", so it must return java.util.List"));
        }
        return Optional.empty();
    }
}
