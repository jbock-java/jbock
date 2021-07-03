package net.jbock.convert;

import dagger.Lazy;
import io.jbock.util.Either;
import net.jbock.convert.matching.AutoConverterFinder;
import net.jbock.convert.matching.ConverterValidator;
import net.jbock.parameter.AbstractItem;

import javax.inject.Inject;

@ParameterScope
public class ConverterFinder {

    private final Lazy<AutoConverterFinder> autoConverterFinder;
    private final Lazy<ConverterValidator> converterValidator;
    private final ConverterClass converterClass;

    @Inject
    ConverterFinder(
            Lazy<AutoConverterFinder> autoConverterFinder,
            Lazy<ConverterValidator> converterValidator,
            ConverterClass converterClass) {
        this.autoConverterFinder = autoConverterFinder;
        this.converterValidator = converterValidator;
        this.converterClass = converterClass;
    }

    public <P extends AbstractItem> Either<String, Mapped<P>> findConverter(P parameter) {
        return converterClass.converter()
                .map(converter -> converterValidator.get().validate(parameter, converter))
                .orElseGet(() -> autoConverterFinder.get().findConverter(parameter));
    }
}