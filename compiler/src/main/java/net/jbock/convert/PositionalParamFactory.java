package net.jbock.convert;

import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.common.ValidationFailure;
import net.jbock.source.SourceMethod;
import net.jbock.source.SourceParameters;

import javax.inject.Inject;

@ConvertScope
public class PositionalParamFactory {

    private final ConverterFinder converterFinder;

    @Inject
    PositionalParamFactory(ConverterFinder converterFinder) {
        this.converterFinder = converterFinder;
    }

    public Either<ValidationFailure, Mapped<AnnotatedParameter>> createPositionalParam(
            SourceMethod<AnnotatedParameter> sourceMethod) {
        return Either.<String, SourceMethod<AnnotatedParameter>>right(sourceMethod)
                .flatMap(converterFinder::findConverter)
                .mapLeft(sourceMethod::fail);
    }

    public Either<ValidationFailure, Mapped<AnnotatedParameters>> createRepeatablePositionalParam(
            SourceParameters sourceMethod) {
        return Either.<String, SourceMethod<AnnotatedParameters>>right(sourceMethod)
                .flatMap(converterFinder::findConverter)
                .mapLeft(sourceMethod::fail);
    }
}
