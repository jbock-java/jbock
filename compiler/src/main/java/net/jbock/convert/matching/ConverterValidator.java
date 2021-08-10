package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.SafeTypes;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapping;
import net.jbock.convert.reference.ReferenceTool;
import net.jbock.convert.reference.StringConverterType;
import net.jbock.util.StringConverter;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import java.util.Optional;
import java.util.function.Supplier;

@ValidateScope
public class ConverterValidator {

    private final ReferenceTool referenceTool;
    private final Util util;
    private final SafeTypes types;

    @Inject
    ConverterValidator(
            ReferenceTool referenceTool,
            Util util,
            SafeTypes types) {
        this.referenceTool = referenceTool;
        this.util = util;
        this.types = types;
    }

    public <M extends AnnotatedMethod>
    Either<ValidationFailure, Mapping<M>> findMapping(
            ValidMatch<M> match,
            TypeElement converter) {
        return referenceTool.getReferencedType(match.sourceMethod(), converter)
                .filter(referencedType -> checkMatchingMatch(match, referencedType))
                .map(referencedType -> {
                    CodeBlock mapExpr = getMapExpr(referencedType, converter);
                    return Mapping.create(mapExpr, match, false);
                });
    }

    private CodeBlock getMapExpr(
            StringConverterType functionType,
            TypeElement converter) {
        if (functionType.isSupplier()) {
            return CodeBlock.of("new $T().get()", converter.asType());
        }
        return CodeBlock.of("new $T()", converter.asType());
    }

    private <M extends AnnotatedMethod>
    Optional<ValidationFailure> checkMatchingMatch(
            ValidMatch<M> match,
            StringConverterType converterType) {
        if (!types.isSameType(converterType.outputType(), match.baseType())) {
            String expectedType = StringConverter.class.getSimpleName() +
                    "<" + util.typeToString(match.baseType()) + ">";
            return Optional.of(match.fail("invalid converter class: should extend " +
                    expectedType + " or implement " +
                    Supplier.class.getSimpleName() + "<" + expectedType + ">"));
        }
        return Optional.empty();
    }
}
