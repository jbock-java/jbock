package net.jbock.convert.matcher;

import com.squareup.javapoet.CodeBlock;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.convert.matching.Match;
import net.jbock.model.Multiplicity;
import net.jbock.source.SourceMethod;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Optional;

@ValidateScope
public class OptionalMatcher implements Matcher {

    private final TypeTool tool;
    private final SafeElements elements;
    private final Types types;

    @Inject
    OptionalMatcher(
            TypeTool tool,
            SafeElements elements,
            Types types) {
        this.tool = tool;
        this.elements = elements;
        this.types = types;
    }

    @Override
    public Optional<Match> tryMatch(SourceMethod<?> parameter) {
        TypeMirror returnType = parameter.returnType();
        return getOptionalPrimitive(returnType)
                .or(() -> // base
                        elements.getTypeElement("java.util.Optional")
                                .flatMap(el -> tool.getSingleTypeArgument(returnType, el)
                                        .map(typeArg -> Match.create(typeArg, Multiplicity.OPTIONAL))))
                .or(() -> // vavr
                        elements.getTypeElement("io.vavr.control.Option")
                                .flatMap(el -> tool.getSingleTypeArgument(returnType, el)
                                        .map(typeArg -> Match.create(typeArg, Multiplicity.OPTIONAL,
                                                CodeBlock.of(".map($1T::of).orElse($1T.none())", types.erasure(el.asType()))))));
    }

    private Optional<Match> getOptionalPrimitive(TypeMirror type) {
        for (OptionalPrimitive optionalPrimitive : OptionalPrimitive.values()) {
            if (tool.isSameType(type, optionalPrimitive.type())) {
                String wrapped = optionalPrimitive.wrappedObjectType();
                return elements.getTypeElement(wrapped)
                        .flatMap(el -> {
                            TypeMirror baseType = el.asType();
                            return Optional.of(Match.create(baseType,
                                    Multiplicity.OPTIONAL, optionalPrimitive.extractExpr()));
                        });
            }
        }
        return Optional.empty();
    }
}
