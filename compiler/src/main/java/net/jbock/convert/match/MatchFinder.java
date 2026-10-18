package net.jbock.convert.match;

import io.jbock.simple.Inject;
import net.jbock.annotated.Item;
import net.jbock.common.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

import static javax.lang.model.type.TypeKind.BOOLEAN;
import static net.jbock.common.TypeTool.AS_PRIMITIVE;
import static net.jbock.model.Multiplicity.OPTIONAL;
import static net.jbock.model.Multiplicity.REQUIRED;

@Inject
public record MatchFinder(
        OptionalMatcher optionalMatcher,
        ListMatcher listMatcher,
        TypeTool tool) {

    public <M extends Item> Match<M> createNullaryMatch(M item) {
        PrimitiveType bool = tool.types().getPrimitiveType(BOOLEAN);
        return Match.create(bool, OPTIONAL, item);
    }

    public <M extends Item> Match<M> findMatch(M item) {
        return optionalMatcher.tryMatch(item)
                .or(() -> listMatcher.tryMatch(item))
                .orElseGet(() -> {
                    TypeMirror baseType = AS_PRIMITIVE.visit(item.returnType())
                            .map(t -> tool.types().boxedClass(t))
                            .map(TypeElement::asType)
                            .orElse(item.returnType());
                    return Match.create(baseType, REQUIRED, item);
                });
    }
}
