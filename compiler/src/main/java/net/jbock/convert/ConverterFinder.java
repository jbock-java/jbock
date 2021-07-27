package net.jbock.convert;

import dagger.Lazy;
import io.jbock.util.Either;
import net.jbock.convert.matching.AutoConverterFinder;
import net.jbock.convert.matching.ConverterValidator;
import net.jbock.parameter.AbstractItem;

import javax.inject.Inject;

@ConvertScope
public class ConverterFinder {

    private final Lazy<AutoConverterFinder> autoConverterFinder;
    private final Lazy<ConverterValidator> converterValidator;
    private final AnnotationUtil annotationUtil;

    @Inject
    ConverterFinder(
            Lazy<AutoConverterFinder> autoConverterFinder,
            Lazy<ConverterValidator> converterValidator,
            AnnotationUtil annotationUtil) {
        this.autoConverterFinder = autoConverterFinder;
        this.converterValidator = converterValidator;
        this.annotationUtil = annotationUtil;
    }

    public <P extends AbstractItem> Either<String, Mapped<P>> findConverter(P parameter) {
        return annotationUtil.getConverter(parameter.sourceMethod().method())
                .map(converter -> converterValidator.get().validate(parameter, converter))
                .orElseGet(() -> autoConverterFinder.get().findConverter(parameter));
    }
}