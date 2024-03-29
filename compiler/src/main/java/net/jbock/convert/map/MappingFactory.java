package net.jbock.convert.map;

import io.jbock.javapoet.CodeBlock;
import io.jbock.simple.Inject;
import io.jbock.util.Either;
import net.jbock.annotated.Item;
import net.jbock.common.SafeTypes;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapping;
import net.jbock.convert.match.Match;
import net.jbock.util.StringConverter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.function.Supplier;

public final class MappingFactory<M extends Item> {

    private final TypeElement converter;
    private final TypeMirror outputType; // the type-arg T in StringConverter<T>
    private final boolean supplier; // true if converter implements Supplier<StringConverter<T>>
    private final SafeTypes types;
    private final Match<M> match;

    private MappingFactory(
            TypeElement converter,
            TypeMirror outputType,
            boolean supplier,
            SafeTypes types,
            Match<M> match) {
        this.converter = converter;
        this.outputType = outputType;
        this.supplier = supplier;
        this.types = types;
        this.match = match;
    }

    Either<ValidationFailure, Mapping<M>> checkMatchingMatch() {
        if (!types.isSameType(outputType, match.baseType())) {
            String expectedType = StringConverter.class.getSimpleName() +
                    "<" + Util.typeToString(match.baseType()) + ">";
            return Either.left(match.fail("invalid converter class: should extend " +
                    expectedType + " or implement " +
                    Supplier.class.getSimpleName() + "<" + expectedType + ">"));
        }
        return Either.right(toMapping());
    }

    private Mapping<M> toMapping() {
        CodeBlock.Builder createConverterExpression = CodeBlock.builder();
        createConverterExpression.add("new $T()", converter.asType());
        if (supplier) {
            createConverterExpression.add(".get()");
        }
        return Mapping.create(createConverterExpression.build(), match);
    }

    public static final class Factory {

        private final SafeTypes types;

        @Inject
        public Factory(TypeTool tool) {
            this.types = tool.types();
        }

        <M extends Item> MappingFactory<M> create(
                TypeElement converter,
                TypeMirror outputType,
                Match<M> match,
                boolean supplier) {
            return new MappingFactory<>(converter, outputType, supplier, types, match);
        }
    }
}
