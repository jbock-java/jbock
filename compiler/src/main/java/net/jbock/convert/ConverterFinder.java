package net.jbock.convert;

import dagger.Lazy;
import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.convert.matching.AutoConverterFinder;
import net.jbock.convert.matching.ConverterValidator;
import net.jbock.source.SourceMethod;

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

    public <M extends AnnotatedMethod> Either<String, Mapped<M>> findConverter(SourceMethod<M> parameter) {
        return annotationUtil.getConverter(parameter.method())
                .map(converter -> converterValidator.get().validate(parameter, converter))
                .orElseGet(() -> autoConverterFinder.get().findConverter(parameter));
    }
}