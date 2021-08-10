package net.jbock.convert;

import dagger.Lazy;
import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.map.AutoOrEnumMapper;
import net.jbock.convert.map.ConverterMapper;
import net.jbock.convert.match.Match;
import net.jbock.convert.match.MatchFinder;
import net.jbock.processor.SourceElement;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static javax.lang.model.element.Modifier.ABSTRACT;

@ValidateScope
public class MappingFinder {

    private final Lazy<AutoOrEnumMapper> autoOrEnumMapper;
    private final Lazy<ConverterMapper> converterValidator;
    private final SourceElement sourceElement;
    private final Util util;
    private final MatchFinder matchFinder;

    @Inject
    MappingFinder(
            Lazy<AutoOrEnumMapper> autoOrEnumMapper,
            Lazy<ConverterMapper> converterValidator,
            SourceElement sourceElement,
            Util util,
            MatchFinder matchFinder) {
        this.autoOrEnumMapper = autoOrEnumMapper;
        this.converterValidator = converterValidator;
        this.sourceElement = sourceElement;
        this.util = util;
        this.matchFinder = matchFinder;
    }

    public <M extends AnnotatedMethod>
    Either<ValidationFailure, Mapping<M>> findMapping(
            M sourceMethod) {
        return matchFinder.findMatch(sourceMethod)
                .flatMap(this::findMappingWithMatch);
    }

    private <M extends AnnotatedMethod>
    Either<ValidationFailure, Mapping<M>> findMappingWithMatch(
            Match<M> match) {
        M sourceMethod = match.sourceMethod();
        return sourceMethod.converter()
                .map(converter -> checkConverterIsInnerClass(sourceMethod, converter)
                        .or(() -> util.commonTypeChecks(converter))
                        .or(() -> checkNotAbstract(sourceMethod, converter))
                        .or(() -> checkNoTypeVars(sourceMethod, converter))
                        .or(() -> checkConverterIsInnerClass(sourceMethod, converter))
                        .map(failure -> failure.prepend("invalid converter class: "))
                        .<Either<ValidationFailure, TypeElement>>map(Either::left)
                        .orElseGet(() -> right(converter))
                        .flatMap(c -> converterValidator.get().findMapping(match, c)))
                .orElseGet(() -> autoOrEnumMapper.get().findMapping(match));
    }

    /* Left-Optional
     */
    private <M extends AnnotatedMethod>
    Optional<ValidationFailure> checkConverterIsInnerClass(
            M sourceMethod,
            TypeElement converter) {
        boolean nestedMapper = util.getEnclosingElements(converter).contains(sourceElement.element());
        if (!nestedMapper) {
            return Optional.of(sourceMethod.fail("converter of '" +
                    sourceMethod.methodName() +
                    "' must be an inner class of the command class '" +
                    sourceElement.element().getSimpleName() + "'"));
        }
        return Optional.empty();
    }

    /* Left-Optional
     */
    private <M extends AnnotatedMethod>
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
    private <M extends AnnotatedMethod>
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