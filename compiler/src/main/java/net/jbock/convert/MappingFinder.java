package net.jbock.convert;

import dagger.Lazy;
import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.matching.AutoValidator;
import net.jbock.convert.matching.ConverterValidator;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;

@ValidateScope
public class MappingFinder {

    private final Lazy<AutoValidator> autoConverterFinder;
    private final Lazy<ConverterValidator> converterValidator;

    @Inject
    MappingFinder(
            Lazy<AutoValidator> autoConverterFinder,
            Lazy<ConverterValidator> converterValidator) {
        this.autoConverterFinder = autoConverterFinder;
        this.converterValidator = converterValidator;
    }

    public <M extends AnnotatedMethod> Either<ValidationFailure, Mapping<M>> findMapping(
            M sourceMethod) {
        return sourceMethod.converter()
                .map(converter -> converterValidator.get().findMapping(sourceMethod, converter))
                .orElseGet(() -> autoConverterFinder.get().findMapping(sourceMethod));
    }
}