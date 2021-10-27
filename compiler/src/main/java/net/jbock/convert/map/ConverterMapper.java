package net.jbock.convert.map;

import io.jbock.util.Either;
import jakarta.inject.Inject;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.SafeElements;
import net.jbock.common.SafeTypes;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapping;
import net.jbock.convert.match.Match;
import net.jbock.util.StringConverter;
import net.jbock.validate.ValidateScope;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Supplier;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;
import static net.jbock.common.TypeTool.AS_DECLARED;

@ValidateScope
public class ConverterMapper {

    private final SafeTypes types;
    private final SafeElements elements;
    private final Util util;

    @Inject
    ConverterMapper(SafeTypes types, SafeElements elements, Util util) {
        this.types = types;
        this.elements = elements;
        this.util = util;
    }

    public <M extends AnnotatedMethod>
    Either<ValidationFailure, Mapping<M>> findMapping(
            Match<M> match,
            TypeElement converter) {
        return getConverterType(match, converter)
                .map(ConverterType::toMapping);
    }

    private <M extends AnnotatedMethod>
    Either<ValidationFailure, ConverterType<M>> getConverterType(
            Match<M> match,
            TypeElement converter) {
        return checkSupplier(converter)
                .map(declaredType -> handleSupplier(converter, match, declaredType))
                .or(() -> checkStringConverter(converter)
                        .map(stringConverterType ->
                                handleStringConverter(converter, match, stringConverterType, false)))
                .orElseGet(() -> left(match.fail(errorConverterType())))
                .filter(referencedType -> referencedType.checkMatchingMatch(util));
    }

    private <M extends AnnotatedMethod>
    Either<ValidationFailure, ConverterType<M>> handleSupplier(
            TypeElement converter,
            Match<M> match,
            DeclaredType declaredType) {
        if (declaredType.getTypeArguments().size() != 1) {
            return left(match.fail(converterRawType(declaredType)));
        }
        TypeMirror typeArgument = declaredType.getTypeArguments().get(0);
        return AS_DECLARED.visit(typeArgument)
                .filter(suppliedFunction -> isSameErasure(suppliedFunction,
                        StringConverter.class))
                .<Either<ValidationFailure, DeclaredType>>map(Either::right)
                .orElseGet(() -> left(match.fail(errorConverterType())))
                .flatMap(suppliedType -> handleStringConverter(converter, match, suppliedType, true));
    }

    private <M extends AnnotatedMethod>
    Either<ValidationFailure, ConverterType<M>> handleStringConverter(
            TypeElement converter,
            Match<M> match,
            DeclaredType declaredType,
            boolean isSupplier) {
        if (declaredType.getTypeArguments().size() != 1) {
            return left(match.fail(converterRawType(declaredType)));
        }
        TypeMirror typeArgument = declaredType.getTypeArguments().get(0);
        return right(new ConverterType<>(converter, match, typeArgument, isSupplier));
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
