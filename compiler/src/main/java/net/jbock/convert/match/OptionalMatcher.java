package net.jbock.convert.match;

import io.jbock.javapoet.CodeBlock;
import net.jbock.annotated.Item;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.convert.match.Match.createWithExtract;
import static net.jbock.model.Multiplicity.OPTIONAL;

final class OptionalMatcher extends Matcher {

    private final TypeTool tool;
    private final SafeElements elements;

    OptionalMatcher(
            TypeTool tool) {
        this.tool = tool;
        this.elements = tool.elements();
    }

    @Override
    <M extends Item>
    Optional<Match<M>> tryMatch(M item) {
        if (item.isVarargsParameter()) {
            return Optional.empty(); // A VarargsParameter cannot match as an Optional.
        }
        TypeMirror returnType = item.returnType();
        return getOptionalPrimitive(item, returnType)
                .or(() -> matchOptional(item, returnType));
    }

    private <M extends Item> Optional<Match<M>>
    matchOptional(M item, TypeMirror returnType) {
        return elements.getTypeElement("java.util.Optional")
                .flatMap(el -> tool.getSingleTypeArgument(returnType, el))
                .map(typeArg -> Match.create(typeArg, OPTIONAL, item));
    }

    private <M extends Item>
    Optional<Match<M>> getOptionalPrimitive(
            M item,
            TypeMirror type) {
        for (OptionalPrimitive optionalPrimitive : OptionalPrimitive.values()) {
            if (tool.isSameType(type, optionalPrimitive.type())) {
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
