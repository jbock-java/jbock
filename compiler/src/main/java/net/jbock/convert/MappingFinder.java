package net.jbock.convert;

import dagger.Lazy;
import io.jbock.javapoet.CodeBlock;
import io.jbock.util.Either;
import jakarta.inject.Inject;
import net.jbock.annotated.Executable;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.map.AutoOrEnumMapper;
import net.jbock.convert.map.ConverterValidator;
import net.jbock.convert.match.Match;
import net.jbock.convert.match.MatchFinder;
import net.jbock.processor.SourceElement;
import net.jbock.util.StringConverter;
import net.jbock.validate.ValidateScope;

import javax.lang.model.element.TypeElement;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static javax.lang.model.element.Modifier.ABSTRACT;

@ValidateScope
public class MappingFinder {

    private final Lazy<AutoOrEnumMapper> autoOrEnumMapper;
    private final Lazy<ConverterValidator> converterValidator;
    private final SourceElement sourceElement;
    private final Util util;
    private final MatchFinder matchFinder;

    @Inject
    MappingFinder(
            Lazy<AutoOrEnumMapper> autoOrEnumMapper,
            Lazy<ConverterValidator> converterValidator,
            SourceElement sourceElement,
            Util util,
            MatchFinder matchFinder) {
        this.autoOrEnumMapper = autoOrEnumMapper;
        this.converterValidator = converterValidator;
        this.sourceElement = sourceElement;
        this.util = util;
        this.matchFinder = matchFinder;
    }

    public <M extends Executable>
    Either<ValidationFailure, Mapping<M>> findMapping(
            M sourceMethod) {
        return matchFinder.findMatch(sourceMethod)
                .flatMap(this::findMappingWithMatch);
    }

    public <M extends Executable>
    Either<ValidationFailure, Mapping<M>> findNullaryMapping(
            M sourceMethod) {
        return matchFinder.createNullaryMatch(sourceMethod)
                .map(match -> Mapping.create(
                        CodeBlock.of("$T.identity())", StringConverter.class), match, true));
    }

    private <M extends Executable>
    Either<ValidationFailure, Mapping<M>> findMappingWithMatch(
            Match<M> match) {
        M sourceMethod = match.sourceMethod();
        return sourceMethod.converter()
                .map(converter -> checkConverterIsInnerClass(sourceMethod, converter)
                        .or(() -> util.commonTypeChecks(converter))
                        .or(() -> checkNotAbstract(sourceMethod, converter))
                        .or(() -> checkNoTypeVars(sourceMethod, converter))
                        .map(failure -> failure.prepend("invalid converter class: "))
                        .<Either<ValidationFailure, TypeElement>>map(Either::left)
                        .orElseGet(() -> right(converter))
                        .flatMap(c -> converterValidator.get().findMapping(match, c)))
                .orElseGet(() -> autoOrEnumMapper.get().findMapping(match));
    }

    /* Left-Optional
     */
    private <M extends Executable>
    Optional<ValidationFailure> checkConverterIsInnerClass(
            M sourceMethod,
            TypeElement converter) {
        boolean nested = util.getEnclosingElements(converter).contains(sourceElement.element());
        if (!nested) {
            return Optional.of(sourceMethod.fail("converter of '" +
                    sourceMethod.methodName() +
                    "' must be an inner class of the command class '" +
                    sourceElement.element().getSimpleName() + "'"));
        }
        return Optional.empty();
    }

    /* Left-Optional
     */
    private <M extends Executable>
    Optional<ValidationFailure> checkNotAbstract(
            M sourceMethod,
            TypeElement converter) {
        if (converter.getModifiers().contains(ABSTRACT)) {
            return Optional.of(sourceMethod.fail("the converter class '" +
                    converter.getSimpleName() +
                    "' may not be abstract"));
        }
        return Optional.empty();
    }

    /* Left-Optional
     */
    private <M extends Executable>
    Optional<ValidationFailure> checkNoTypeVars(
            M sourceMethod,
            TypeElement converter) {
        if (!converter.getTypeParameters().isEmpty()) {
            return Optional.of(sourceMethod.fail("type parameters are not allowed in the declaration of" +
                    " converter class '" +
                    converter.getSimpleName() +
                    "'"));
        }
        return Optional.empty();
    }
}