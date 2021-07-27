package net.jbock.convert;

import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.common.ValidationFailure;
import net.jbock.parameter.PositionalParameter;
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

    public Either<ValidationFailure, Mapped<PositionalParameter>> createPositionalParam(
            SourceMethod<AnnotatedParameter> sourceMethod) {
        int position = sourceMethod.annotatedMethod().index();
        PositionalParameter positionalParameter = new PositionalParameter(sourceMethod, position);
        return Either.<String, PositionalParameter>right(positionalParameter)
                .flatMap(converterFinder::findConverter)
                .mapLeft(sourceMethod::fail);
    }

    public Either<ValidationFailure, Mapped<PositionalParameter>> createRepeatablePositionalParam(
            SourceParameters sourceMethod) {
        int position = sourceMethod.index();
        PositionalParameter positionalParameter = new PositionalParameter(sourceMethod, position);
        return Either.<String, PositionalParameter>right(positionalParameter)
                .flatMap(converterFinder::findConverter)
                .mapLeft(sourceMethod::fail);
    }
}
