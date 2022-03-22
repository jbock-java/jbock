package net.jbock.convert.match;

import io.jbock.javapoet.CodeBlock;
import javax.inject.Inject;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.validate.ValidateScope;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.convert.match.Match.createWithExtract;
import static net.jbock.model.Multiplicity.OPTIONAL;

@ValidateScope
public class OptionalMatcher implements Matcher {

    private final TypeTool tool;
    private final SafeElements elements;

    @Inject
    OptionalMatcher(
            TypeTool tool,
            SafeElements elements) {
        this.tool = tool;
        this.elements = elements;
    }

    @Override
    public <M extends AnnotatedMethod>
    Optional<Match<M>> tryMatch(
            M sourceMethod) {
        TypeMirror returnType = sourceMethod.returnType();
        return getOptionalPrimitive(sourceMethod, returnType)
                .or(() -> matchOptional(sourceMethod, returnType));
    }

    private <M extends AnnotatedMethod> Optional<Match<M>>
    matchOptional(M sourceMethod, TypeMirror returnType) {
        return elements.getTypeElement("java.util.Optional")
                .flatMap(el -> tool.getSingleTypeArgument(returnType, el))
                .map(typeArg -> Match.create(typeArg, OPTIONAL, sourceMethod));
    }

    private <M extends AnnotatedMethod>
    Optional<Match<M>> getOptionalPrimitive(
            M sourceMethod,
            TypeMirror type) {
        for (OptionalPrimitive optionalPrimitive : OptionalPrimitive.values()) {
            if (tool.isSameType(type, optionalPrimitive.type())) {
                CodeBlock extractExpr = optionalPrimitive.extractExpr();
                return elements.getTypeElement(optionalPrimitive.numberType())
                        .map(TypeElement::asType)
                        .map(numberType ->
                                createWithExtract(numberType, extractExpr, sourceMethod));
            }
        }
        return Optional.empty();
    }
}
