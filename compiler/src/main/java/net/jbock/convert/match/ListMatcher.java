package net.jbock.convert.match;

import jakarta.inject.Inject;
import net.jbock.annotated.Item;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.validate.ValidateScope;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.model.Multiplicity.REPEATABLE;

@ValidateScope
class ListMatcher extends Matcher {

    private final SafeElements elements;
    private final TypeTool tool;

    @Inject
    ListMatcher(
            SafeElements elements,
            TypeTool tool) {
        this.elements = elements;
        this.tool = tool;
    }

    @Override
    <M extends Item>
    Optional<Match<M>> tryMatch(
            M sourceMethod) {
        if (sourceMethod.isParameter()) {
            return Optional.empty(); // Not a VarargsParameter, so definitely not repeatable.
        }
        TypeMirror returnType = sourceMethod.returnType();
        return elements.getTypeElement("java.util.List")
                .flatMap(utilList -> tool.getSingleTypeArgument(returnType, utilList))
                .map(typeArg -> Match.create(typeArg, REPEATABLE, sourceMethod));
    }
}
