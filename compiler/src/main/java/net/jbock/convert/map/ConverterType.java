package net.jbock.convert.map;

import com.squareup.javapoet.CodeBlock;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapping;
import net.jbock.convert.match.Match;
import net.jbock.util.StringConverter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Supplier;

public final class ConverterType<M extends AnnotatedMethod> {

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

    Optional<ValidationFailure> checkMatchingMatch(
            Util util) {
        if (!util.types().isSameType(outputType, match.baseType())) {
            String expectedType = StringConverter.class.getSimpleName() +
                    "<" + util.typeToString(match.baseType()) + ">";
            return Optional.of(match.fail("invalid converter class: should extend " +
                    expectedType + " or implement " +
                    Supplier.class.getSimpleName() + "<" + expectedType + ">"));
        }
        return Optional.empty();
    }

    private CodeBlock asMapper() {
        TypeMirror type = converter.asType();
        if (supplier) {
            return CodeBlock.of("new $T().get()", type);
        }
        return CodeBlock.of("new $T()", type);
    }

    Mapping<M> toMapping() {
        return Mapping.create(asMapper(), match);
    }
}
