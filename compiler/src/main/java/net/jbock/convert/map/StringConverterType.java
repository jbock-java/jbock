package net.jbock.convert.map;

import com.squareup.javapoet.CodeBlock;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.match.Match;
import net.jbock.util.StringConverter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Supplier;

final class StringConverterType {

    private final TypeMirror outputType; // the type-arg T in StringConverter<T>
    private final boolean supplier; // true if implements Supplier<StringConverter<T>>

    StringConverterType(TypeMirror outputType, boolean supplier) {
        this.outputType = outputType;
        this.supplier = supplier;
    }

    <M extends AnnotatedMethod>
    Optional<ValidationFailure> checkMatchingMatch(
            Match<M> match,
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

    CodeBlock getMapExpr(TypeElement converter) {
        if (supplier) {
            return CodeBlock.of("new $T().get()", converter.asType());
        }
        return CodeBlock.of("new $T()", converter.asType());
    }
}
