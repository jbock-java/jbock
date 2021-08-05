package net.jbock.convert.reference;

import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.TypeTool;
import net.jbock.common.ValidationFailure;
import net.jbock.util.StringConverter;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Supplier;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;
import static net.jbock.common.TypeTool.AS_DECLARED;

@ValidateScope
public class ReferenceTool {

    private final TypeTool tool;

    @Inject
    ReferenceTool(TypeTool tool) {
        this.tool = tool;
    }

    public <M extends AnnotatedMethod>
    Either<ValidationFailure, StringConverterType> getReferencedType(
            M sourceMethod,
            TypeElement converter) {
        Optional<DeclaredType> supplier = checkSupplier(converter);
        Optional<DeclaredType> stringConverter = checkStringConverter(converter);
        if (supplier.isPresent() && stringConverter.isPresent()) {
            return left(sourceMethod.fail(errorConverterType() + " but not both"));
        }
        return supplier.map(declaredType -> handleSupplier(sourceMethod, declaredType))
                .or(() -> stringConverter.map(stringConverterType ->
                        handleStringConverter(sourceMethod, stringConverterType, false)))
                .orElseGet(() -> left(sourceMethod.fail(errorConverterType())));
    }

    private <M extends AnnotatedMethod>
    Either<ValidationFailure, StringConverterType> handleSupplier(
            M sourceMethod,
            DeclaredType declaredType) {
        if (declaredType.getTypeArguments().size() != 1) {
            return left(sourceMethod.fail(converterRawType()));
        }
        TypeMirror typeArgument = declaredType.getTypeArguments().get(0);
        return AS_DECLARED.visit(typeArgument)
                .filter(suppliedFunction -> tool.isSameErasure(suppliedFunction,
                        StringConverter.class))
                .<Either<ValidationFailure, DeclaredType>>map(Either::right)
                .orElseGet(() -> left(sourceMethod.fail(errorConverterType())))
                .flatMap(suppliedType -> handleStringConverter(sourceMethod, suppliedType, true));
    }

    private <M extends AnnotatedMethod>
    Either<ValidationFailure, StringConverterType> handleStringConverter(
            M parameter,
            DeclaredType declaredType,
            boolean isSupplier) {
        if (declaredType.getTypeArguments().size() != 1) {
            return left(parameter.fail(converterRawType()));
        }
        TypeMirror typeArgument = declaredType.getTypeArguments().get(0);
        return right(new StringConverterType(typeArgument, isSupplier));
    }

    private Optional<DeclaredType> checkSupplier(TypeElement converter) {
        return converter.getInterfaces().stream()
                .filter(inter -> tool.isSameErasure(inter, Supplier.class))
                .map(AS_DECLARED::visit)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private Optional<DeclaredType> checkStringConverter(TypeElement converter) {
        return Optional.of(converter.getSuperclass())
                .filter(inter -> tool.isSameErasure(inter, StringConverter.class))
                .flatMap(AS_DECLARED::visit);
    }

    private String errorConverterType() {
        return "invalid converter class: converter must extend " + StringConverter.class.getSimpleName() +
                "<X> or implement " + Supplier.class.getSimpleName() +
                "<" + StringConverter.class.getSimpleName() + "<X>>";
    }

    private String converterRawType() {
        return "raw type in converter class";
    }
}
