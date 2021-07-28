package net.jbock.convert;

import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.common.ValidationFailure;
import net.jbock.source.SourceOption;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static javax.lang.model.type.TypeKind.BOOLEAN;
import static net.jbock.convert.Mapped.createFlag;

@ConvertScope
public class NamedOptionFactory {

    private final ConverterFinder converterFinder;
    private final Types types;

    @Inject
    NamedOptionFactory(
            ConverterFinder converterFinder,
            Types types) {
        this.converterFinder = converterFinder;
        this.types = types;
    }

    public Either<ValidationFailure, Mapped<AnnotatedOption>> createNamedOption(SourceOption sourceMethod) {
        Optional<TypeElement> converter = sourceMethod.annotatedMethod().converter();
        if (converter.isEmpty() && sourceMethod.returnType().getKind() == BOOLEAN) {
            return right(createFlag(sourceMethod, types.getPrimitiveType(BOOLEAN)));
        }
        return converterFinder.findConverter(sourceMethod).mapLeft(sourceMethod::fail);
    }
}
