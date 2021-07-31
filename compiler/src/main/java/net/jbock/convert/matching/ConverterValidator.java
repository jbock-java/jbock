package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import io.jbock.util.Either;
import net.jbock.Converter;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapping;
import net.jbock.convert.reference.ReferenceTool;
import net.jbock.convert.reference.StringConverterType;
import net.jbock.processor.SourceElement;
import net.jbock.source.SourceMethod;
import net.jbock.util.StringConverter;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import java.util.Optional;

import static javax.lang.model.element.Modifier.ABSTRACT;

@ValidateScope
public class ConverterValidator {

    private final ReferenceTool referenceTool;
    private final Util util;
    private final SourceElement sourceElement;
    private final Types types;
    private final MatchFinder matchFinder;

    @Inject
    ConverterValidator(
            ReferenceTool referenceTool,
            Util util,
            SourceElement sourceElement,
            Types types,
            MatchFinder matchFinder) {
        this.referenceTool = referenceTool;
        this.util = util;
        this.sourceElement = sourceElement;
        this.types = types;
        this.matchFinder = matchFinder;
    }

    public <M extends AnnotatedMethod> Either<ValidationFailure, Mapping<M>> findMapping(
            SourceMethod<M> sourceMethod,
            TypeElement converter) {
        return util.commonTypeChecks(converter)
                .map(sourceMethod::fail)
                .or(() -> checkNotAbstract(sourceMethod, converter))
                .or(() -> checkNoTypevars(sourceMethod, converter))
                .or(() -> checkConverterAnnotationPresent(sourceMethod, converter))
                .<Either<ValidationFailure, StringConverterType>>map(Either::left)
                .orElseGet(() -> referenceTool.getReferencedType(sourceMethod, converter))
                .mapLeft(failure -> failure.prepend("invalid converter class: "))
                .flatMap(functionType -> tryAllMatchers(functionType, sourceMethod, converter));
    }

    private <M extends AnnotatedMethod> Either<ValidationFailure, Mapping<M>> tryAllMatchers(
            StringConverterType functionType,
            SourceMethod<M> sourceMethod,
            TypeElement converter) {
        return matchFinder.findMatch(sourceMethod)
                .filter(match -> isValidMatch(match, functionType))
                .map(match -> Mapping.create(getMapExpr(functionType, converter), match, false));
    }

    /* Left-Optional
     */
    private <M extends AnnotatedMethod> Optional<ValidationFailure> checkConverterAnnotationPresent(
            SourceMethod<M> sourceMethod,
            TypeElement converter) {
        Converter converterAnnotation = converter.getAnnotation(Converter.class);
        boolean nestedMapper = util.getEnclosingElements(converter).contains(sourceElement.element());
        if (converterAnnotation == null && !nestedMapper) {
            return Optional.of(sourceMethod.fail("converter must be an inner class of the command class, or carry the @"
                    + Converter.class.getSimpleName() + " annotation"));
        }
        return Optional.empty();
    }

    /* Left-Optional
     */
    private <M extends AnnotatedMethod> Optional<ValidationFailure> checkNotAbstract(
            SourceMethod<M> sourceMethod,
            TypeElement converter) {
        if (converter.getModifiers().contains(ABSTRACT)) {
            return Optional.of(sourceMethod.fail("converter class may not be abstract"));
        }
        return Optional.empty();
    }

    /* Left-Optional
     */
    private <M extends AnnotatedMethod> Optional<ValidationFailure> checkNoTypevars(
            SourceMethod<M> sourceMethod,
            TypeElement converter) {
        if (!converter.getTypeParameters().isEmpty()) {
            return Optional.of(sourceMethod.fail("type parameters are not allowed in converter class declaration"));
        }
        return Optional.empty();
    }

    private CodeBlock getMapExpr(
            StringConverterType functionType,
            TypeElement converter) {
        if (functionType.isSupplier()) {
            return CodeBlock.of("new $T().get()", converter.asType());
        }
        return CodeBlock.of("new $T()", converter.asType());
    }

    private <M extends AnnotatedMethod> Optional<ValidationFailure> isValidMatch(
            ValidMatch<M> match,
            StringConverterType functionType) {
        if (!types.isSameType(functionType.outputType(), match.baseType())) {
            return Optional.of(match.sourceMethod().fail("invalid converter class: should extend " +
                    StringConverter.class.getSimpleName() +
                    "<" + util.typeToString(match.baseType()) + ">"));
        }
        return Optional.empty();
    }
}
