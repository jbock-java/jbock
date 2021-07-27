package net.jbock.convert;

import io.jbock.util.Either;
import net.jbock.common.ValidationFailure;
import net.jbock.parameter.NamedOption;
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
    private final AnnotationUtil annotationUtil;

    @Inject
    NamedOptionFactory(
            ConverterFinder converterFinder,
            Types types,
            AnnotationUtil annotationUtil) {
        this.converterFinder = converterFinder;
        this.types = types;
        this.annotationUtil = annotationUtil;
    }

    public Either<ValidationFailure, Mapped<NamedOption>> createNamedOption(SourceOption sourceMethod) {
        NamedOption namedOption = new NamedOption(sourceMethod.names(), sourceMethod);
        Optional<TypeElement> converter = annotationUtil.getConverter(sourceMethod.method());
        if (converter.isEmpty() && sourceMethod.returnType().getKind() == BOOLEAN) {
            return right(createFlag(namedOption, types.getPrimitiveType(BOOLEAN)));
        }
        return converterFinder.findConverter(namedOption).mapLeft(sourceMethod::fail);
    }
}
