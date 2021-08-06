package net.jbock.convert.matcher;

import com.squareup.javapoet.CodeBlock;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.SafeElements;
import net.jbock.common.SafeTypes;
import net.jbock.common.TypeTool;
import net.jbock.convert.matching.Match;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.model.Multiplicity.OPTIONAL;

@ValidateScope
public class OptionalMatcher implements Matcher {

    private final TypeTool tool;
    private final SafeElements elements;
    private final SafeTypes types;

    @Inject
    OptionalMatcher(
            TypeTool tool,
            SafeElements elements,
            SafeTypes types) {
        this.tool = tool;
        this.elements = elements;
        this.types = types;
    }

    @Override
    public <M extends AnnotatedMethod>
    Optional<Match<M>> tryMatch(
            M parameter) {
        TypeMirror returnType = parameter.returnType();
        return getOptionalPrimitive(parameter, returnType)
                .or(() -> // base
                        elements.getTypeElement("java.util.Optional")
                                .flatMap(el -> tool.getSingleTypeArgument(returnType, el)
                                        .map(typeArg -> Match.create(typeArg, OPTIONAL, parameter))))
                .or(() -> // vavr
                        elements.getTypeElement("io.vavr.control.Option")
                                .flatMap(el -> tool.getSingleTypeArgument(returnType, el)
                                        .map(typeArg -> Match.create(typeArg, OPTIONAL,
                                                CodeBlock.of(".map($1T::of).orElse($1T.none())", types.erasure(el.asType())), parameter))));
    }

    private <M extends AnnotatedMethod>
    Optional<Match<M>> getOptionalPrimitive(
            M parameter,
            TypeMirror type) {
        for (OptionalPrimitive optionalPrimitive : OptionalPrimitive.values()) {
            if (tool.isSameType(type, optionalPrimitive.type())) {
                CodeBlock extractExpr = optionalPrimitive.extractExpr();
                return elements.getTypeElement(optionalPrimitive.numberType())
                        .map(TypeElement::asType)
                        .flatMap(numberType ->
                                Optional.of(Match.create(numberType, OPTIONAL, extractExpr, parameter)));
            }
        }
        return Optional.empty();
    }
}
