package net.jbock.convert.reference;

import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.SafeElements;
import net.jbock.common.SafeTypes;
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

    private final SafeTypes types;
    private final SafeElements elements;

    @Inject
    ReferenceTool(SafeTypes types, SafeElements elements) {
        this.types = types;
        this.elements = elements;
    }

    public <M extends AnnotatedMethod>
    Either<ValidationFailure, StringConverterType> getReferencedType(
            M sourceMethod,
            TypeElement converter) {
        return checkSupplier(converter)
                .map(declaredType -> handleSupplier(sourceMethod, declaredType))
                .or(() -> checkStringConverter(converter).map(stringConverterType ->
                        handleStringConverter(sourceMethod, stringConverterType, false)))
                .orElseGet(() -> left(sourceMethod.fail(errorConverterType())));
    }

    private <M extends AnnotatedMethod>
    Either<ValidationFailure, StringConverterType> handleSupplier(
            M sourceMethod,
            DeclaredType declaredType) {
        if (declaredType.getTypeArguments().size() != 1) {
            return left(sourceMethod.fail(converterRawType(declaredType)));
        }
        TypeMirror typeArgument = declaredType.getTypeArguments().get(0);
        return AS_DECLARED.visit(typeArgument)
                .filter(suppliedFunction -> isSameErasure(suppliedFunction,
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
            return left(parameter.fail(converterRawType(declaredType)));
        }
        TypeMirror typeArgument = declaredType.getTypeArguments().get(0);
        return right(new StringConverterType(typeArgument, isSupplier));
    }

    private Optional<DeclaredType> checkSupplier(TypeElement converter) {
        return converter.getInterfaces().stream()
                .filter(inter -> isSameErasure(inter, Supplier.class))
                .map(AS_DECLARED::visit)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private Optional<DeclaredType> checkStringConverter(TypeElement converter) {
        return Optional.of(converter.getSuperclass())
                .filter(inter -> isSameErasure(inter, StringConverter.class))
                .flatMap(AS_DECLARED::visit);
    }

    private boolean isSameErasure(TypeMirror x, Class<?> y) {
        return elements.getTypeElement(y.getCanonicalName())
                .map(TypeElement::asType)
                .map(type -> types.isSameType(types.erasure(x), types.erasure(type)))
                .orElse(false);
    }

    private String errorConverterType() {
        return "invalid converter class: converter must extend " + StringConverter.class.getSimpleName() +
                "<X> or implement " + Supplier.class.getSimpleName() +
                "<" + StringConverter.class.getSimpleName() + "<X>>";
    }

    private String converterRawType(DeclaredType declaredType) {
        return "invalid converter class: missing a type parameter in type '" +
                declaredType.asElement().getSimpleName() + "'";
    }
}
