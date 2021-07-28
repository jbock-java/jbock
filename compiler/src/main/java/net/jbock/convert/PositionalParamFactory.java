package net.jbock.convert;

import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.ValidationFailure;
import net.jbock.source.SourceMethod;

import javax.inject.Inject;

@ConvertScope
public class PositionalParamFactory {

    private final ConverterFinder converterFinder;

    @Inject
    PositionalParamFactory(ConverterFinder converterFinder) {
        this.converterFinder = converterFinder;
    }

    public <M extends AnnotatedMethod> Either<ValidationFailure, Mapped<M>> createPositionalParam(
            SourceMethod<M> sourceMethod) {
        return converterFinder.findConverter(sourceMethod).mapLeft(sourceMethod::fail);
    }
}
