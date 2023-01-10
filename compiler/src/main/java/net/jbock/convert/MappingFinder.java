package net.jbock.convert;

import io.jbock.javapoet.CodeBlock;
import io.jbock.util.Either;
import net.jbock.annotated.Item;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.map.AutoOrEnumMapper;
import net.jbock.convert.map.ConverterValidator;
import net.jbock.convert.match.Match;
import net.jbock.convert.match.MatchFinder;
import net.jbock.processor.SourceElement;
import net.jbock.util.StringConverter;

import javax.lang.model.element.TypeElement;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static javax.lang.model.element.Modifier.ABSTRACT;

public final class MappingFinder {

    private final AutoOrEnumMapper autoOrEnumMapper;
    private final ConverterValidator converterValidator;
    private final SourceElement sourceElement;
    private final Util util;
    private final MatchFinder matchFinder;

    public MappingFinder(
            AutoOrEnumMapper autoOrEnumMapper,
            ConverterValidator converterValidator,
            SourceElement sourceElement,
            Util util,
            MatchFinder matchFinder) {
        this.autoOrEnumMapper = autoOrEnumMapper;
        this.converterValidator = converterValidator;
        this.sourceElement = sourceElement;
        this.util = util;
        this.matchFinder = matchFinder;
    }

    public <M extends Item>
    Either<ValidationFailure, Mapping<M>> findMapping(M item) {
        return matchFinder.findMatch(item)
                .flatMap(this::findMappingWithMatch);
    }

    public <M extends Item>
    Either<ValidationFailure, Mapping<M>> findNullaryMapping(M item) {
        return matchFinder.createNullaryMatch(item)
                .map(match -> Mapping.create(
                        CodeBlock.of("$T.identity())", StringConverter.class), match, true));
    }

    private <M extends Item>
    Either<ValidationFailure, Mapping<M>> findMappingWithMatch(Match<M> match) {
        return match.item().converter()
                .map(converter -> checkConverterIsInnerClass(match.item(), converter)
                        .or(() -> util.commonTypeChecks(converter))
                        .or(() -> checkNotAbstract(match.item(), converter))
                        .or(() -> checkNoTypeVars(match.item(), converter))
                        .map(failure -> failure.prepend("invalid converter class: "))
                        .<Either<ValidationFailure, TypeElement>>map(Either::left)
                        .orElseGet(() -> right(converter))
                        .flatMap(c -> converterValidator.findMapping(match, c)))
                .orElseGet(() -> autoOrEnumMapper.findMapping(match));
    }

    /* Left-Optional
     */
    private <M extends Item>
    Optional<ValidationFailure> checkConverterIsInnerClass(
            M item,
            TypeElement converter) {
        boolean nested = util.getEnclosingElements(converter).contains(sourceElement.element());
        if (!nested) {
            return Optional.of(item.fail("converter of '" +
                    item.methodName() +
                    "' must be an inner class of the command class '" +
                    sourceElement.element().getSimpleName() + "'"));
        }
        return Optional.empty();
    }

    /* Left-Optional
     */
    private <M extends Item>
    Optional<ValidationFailure> checkNotAbstract(
            M item,
            TypeElement converter) {
        if (converter.getModifiers().contains(ABSTRACT)) {
            return Optional.of(item.fail("the converter class '" +
                    converter.getSimpleName() +
                    "' may not be abstract"));
        }
        return Optional.empty();
    }

    /* Left-Optional
     */
    private <M extends Item>
    Optional<ValidationFailure> checkNoTypeVars(
            M item,
            TypeElement converter) {
        if (!converter.getTypeParameters().isEmpty()) {
            return Optional.of(item.fail("type parameters are not allowed in the declaration of" +
                    " converter class '" +
                    converter.getSimpleName() +
                    "'"));
        }
        return Optional.empty();
    }
}