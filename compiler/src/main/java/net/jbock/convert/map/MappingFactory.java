package net.jbock.convert.map;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import io.jbock.javapoet.CodeBlock;
import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.SafeTypes;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapping;
import net.jbock.convert.match.Match;
import net.jbock.util.StringConverter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.function.Supplier;

final class MappingFactory {

    private final TypeElement converter;
    private final TypeMirror outputType; // the type-arg T in StringConverter<T>
    private final boolean supplier; // true if converter implements Supplier<StringConverter<T>>
    private final SafeTypes types;

    @AssistedInject
    MappingFactory(
            @Assisted TypeElement converter,
            @Assisted TypeMirror outputType,
            @Assisted boolean supplier,
            SafeTypes types) {
        this.converter = converter;
        this.outputType = outputType;
        this.supplier = supplier;
        this.types = types;
    }

    <M extends AnnotatedMethod<?>> Either<ValidationFailure, Mapping<M>> checkMatchingMatch(Match<M> match) {
        if (!types.isSameType(outputType, match.baseType())) {
            String expectedType = StringConverter.class.getSimpleName() +
                    "<" + Util.typeToString(match.baseType()) + ">";
            return Either.left(match.fail("invalid converter class: should extend " +
                    expectedType + " or implement " +
                    Supplier.class.getSimpleName() + "<" + expectedType + ">"));
        }
        return Either.right(toMapping(match));
    }

    private <M extends AnnotatedMethod<?>> Mapping<M> toMapping(Match<M> match) {
        CodeBlock.Builder createConverterExpression = CodeBlock.builder();
        createConverterExpression.add("new $T()", converter.asType());
        if (supplier) {
            createConverterExpression.add(".get()");
        }
        return Mapping.create(createConverterExpression.build(), match);
    }

    @AssistedFactory
    interface Factory {
        MappingFactory create(TypeElement converter, TypeMirror outputType, boolean supplier);
    }
}
