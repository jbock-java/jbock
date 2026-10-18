package net.jbock.convert.match;

import io.jbock.simple.Inject;
import net.jbock.annotated.Item;
import net.jbock.common.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.model.Multiplicity.REPEATABLE;

@Inject
public record ListMatcher(TypeTool tool) {

    <M extends Item> Optional<Match<M>> tryMatch(M item) {
        TypeMirror returnType = item.returnType();
        return tool.getSingleTypeArgument(returnType, java.util.List.class)
                .map(typeArg -> Match.create(typeArg, REPEATABLE, item));
    }
}
