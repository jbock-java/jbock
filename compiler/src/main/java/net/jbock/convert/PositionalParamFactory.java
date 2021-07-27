package net.jbock.convert;

import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.common.ValidationFailure;
import net.jbock.parameter.PositionalParameter;
import net.jbock.processor.SourceElement;
import net.jbock.source.SourceMethod;
import net.jbock.source.SourceParameters;

import javax.inject.Inject;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;

@ConvertScope
public class PositionalParamFactory {

    private final ConverterFinder converterFinder;
    private final SourceElement sourceElement;

    @Inject
    PositionalParamFactory(
            ConverterFinder converterFinder,
            SourceElement sourceElement) {
        this.converterFinder = converterFinder;
        this.sourceElement = sourceElement;
    }

    public Either<ValidationFailure, Mapped<PositionalParameter>> createPositionalParam(
            SourceMethod<AnnotatedParameter> sourceMethod) {
        int position = sourceMethod.annotatedMethod().index();
        PositionalParameter positionalParameter = new PositionalParameter(sourceMethod, position);
        return Either.<String, PositionalParameter>right(positionalParameter)
                .flatMap(converterFinder::findConverter)
                .flatMap(this::checkPositionNotNegative)
                .flatMap(this::checkSuperNotRepeatable)
                .mapLeft(sourceMethod::fail);
    }

    public Either<ValidationFailure, Mapped<PositionalParameter>> createRepeatablePositionalParam(
            SourceParameters sourceMethod) {
        int position = sourceMethod.getIndex();
        PositionalParameter positionalParameter = new PositionalParameter(sourceMethod, position);
        return Either.<String, PositionalParameter>right(positionalParameter)
                .flatMap(converterFinder::findConverter)
                .flatMap(this::checkPositionNotNegative)
                .flatMap(this::checkSuperNotRepeatable)
                .mapLeft(sourceMethod::fail);
    }

    private Either<String, Mapped<PositionalParameter>> checkPositionNotNegative(
            Mapped<PositionalParameter> c) {
        PositionalParameter p = c.item();
        if (p.position() < 0) {
            return left("negative positions are not allowed");
        }
        return right(c);
    }

    private Either<String, Mapped<PositionalParameter>> checkSuperNotRepeatable(Mapped<PositionalParameter> c) {
        if (sourceElement.isSuperCommand() && c.isRepeatable()) {
            return left("positional parameter may not be repeatable when the superCommand" +
                    " attribute is set");
        }
        return right(c);
    }
}
