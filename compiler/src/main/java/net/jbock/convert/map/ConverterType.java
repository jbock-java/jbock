package net.jbock.convert.map;

import io.jbock.javapoet.CodeBlock;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.SafeTypes;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapping;
import net.jbock.convert.match.Match;
import net.jbock.util.StringConverter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Supplier;

final class ConverterType<M extends AnnotatedMethod> {

    private final TypeElement converter;
    private final Match<M> match;
    private final TypeMirror outputType; // the type-arg T in StringConverter<T>
    private final boolean supplier; // true if implements Supplier<StringConverter<T>>

    ConverterType(
            TypeElement converter,
            Match<M> match,
            TypeMirror outputType,
            boolean supplier) {
        this.converter = converter;
        this.match = match;
        this.outputType = outputType;
        this.supplier = supplier;
    }

    /* Left-Optional
     */
    Optional<ValidationFailure> checkMatchingMatch(
            SafeTypes types) {
        if (!types.isSameType(outputType, match.baseType())) {
            String expectedType = StringConverter.class.getSimpleName() +
                    "<" + Util.typeToString(match.baseType()) + ">";
            return Optional.of(match.fail("invalid converter class: should extend " +
                    expectedType + " or implement " +
                    Supplier.class.getSimpleName() + "<" + expectedType + ">"));
        }
        return Optional.empty();
    }

    Mapping<M> toMapping() {
        CodeBlock.Builder createConverterExpression = CodeBlock.builder();
        createConverterExpression.add("new $T()", converter.asType());
        if (supplier) {
            createConverterExpression.add(".get()");
        }
        return Mapping.create(createConverterExpression.build(), match);
    }
}
