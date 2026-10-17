package net.jbock.convert.match;

import com.palantir.javapoet.CodeBlock;
import net.jbock.annotated.Item;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.convert.match.Match.createWithExtract;
import static net.jbock.model.Multiplicity.OPTIONAL;

public final class OptionalMatcher extends Matcher {

    private final TypeTool tool;
    private final SafeElements elements;

    public OptionalMatcher(
            TypeTool tool) {
        this.tool = tool;
        this.elements = tool.elements();
    }

    @Override
    <M extends Item>
    Optional<Match<M>> tryMatch(M item) {
        TypeMirror returnType = item.returnType();
        return getOptionalPrimitive(item, returnType)
                .or(() -> matchOptional(item, returnType));
    }

    private <M extends Item> Optional<Match<M>>
    matchOptional(M item, TypeMirror returnType) {
        return tool.getSingleTypeArgument(returnType, java.util.Optional.class)
                .map(typeArg -> Match.create(typeArg, OPTIONAL, item));
    }

    private <M extends Item>
    Optional<Match<M>> getOptionalPrimitive(
            M item,
            TypeMirror type) {
        for (OptionalPrimitive optionalPrimitive : OptionalPrimitive.values()) {
            if (tool.isErasureOf(type, optionalPrimitive.type())) {
                CodeBlock extractExpr = optionalPrimitive.extractExpr();
                return elements.getTypeElement(optionalPrimitive.numberType())
                        .map(TypeElement::asType)
                        .map(numberType ->
                                createWithExtract(numberType, extractExpr, item));
            }
        }
        return Optional.empty();
    }
}
