package net.jbock.convert;

import io.jbock.util.Either;
import net.jbock.common.ValidationFailure;
import net.jbock.parameter.PositionalParameter;
import net.jbock.parameter.SourceMethod;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;

@ConvertScope
public class PositionalParamFactory {

    private final ConverterFinder converterFinder;
    private final SourceMethod sourceMethod;
    private final SourceElement sourceElement;

    @Inject
    PositionalParamFactory(
            ConverterFinder converterFinder,
            SourceMethod sourceMethod,
            SourceElement sourceElement) {
        this.converterFinder = converterFinder;
        this.sourceMethod = sourceMethod;
        this.sourceElement = sourceElement;
    }

    public Either<ValidationFailure, Mapped<PositionalParameter>> createPositionalParam(int position) {
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
