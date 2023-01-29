package net.jbock.convert.match;

import io.jbock.simple.Inject;
import net.jbock.annotated.Item;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.model.Multiplicity.REPEATABLE;

public final class ListMatcher extends Matcher {

    private final SafeElements elements;
    private final TypeTool tool;

    @Inject
    public ListMatcher(
            TypeTool tool) {
        this.elements = tool.elements();
        this.tool = tool;
    }

    @Override
    <M extends Item>
    Optional<Match<M>> tryMatch(M item) {
        if (item.isParameter()) {
            return Optional.empty(); // Not a VarargsParameter, so definitely not repeatable.
        }
        TypeMirror returnType = item.returnType();
        return elements.getTypeElement("java.util.List")
                .flatMap(utilList -> tool.getSingleTypeArgument(returnType, utilList))
                .map(typeArg -> Match.create(typeArg, REPEATABLE, item));
    }
}
