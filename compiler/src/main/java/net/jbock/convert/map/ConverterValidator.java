package net.jbock.convert.map;

import io.jbock.simple.Inject;
import io.jbock.util.Either;
import net.jbock.annotated.Item;
import net.jbock.common.TypeTool;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapping;
import net.jbock.convert.match.Match;
import net.jbock.util.StringConverter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Supplier;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;
import static net.jbock.common.TypeTool.AS_DECLARED;

public final class ConverterValidator {

    private final TypeTool tool;
    private final MappingFactory.Factory mappingFactoryFactory;

    @Inject
    public ConverterValidator(
            TypeTool tool,
            MappingFactory.Factory mappingFactoryFactory) {
        this.tool = tool;
        this.mappingFactoryFactory = mappingFactoryFactory;
    }

    public <M extends Item>
    Either<ValidationFailure, Mapping<M>> findMapping(
            Match<M> match,
            TypeElement converter) {
        return checkSuppliedConverter(converter, match)
                .or(() -> checkDirectConverter(converter, match))
                .orElseGet(() -> left(match.fail(errorConverterType())))
                .flatMap(MappingFactory::checkMatchingMatch);
    }

    private <M extends Item>
    Either<ValidationFailure, MappingFactory<M>> handleConverter(
            TypeElement converter,
            Match<M> match,
            DeclaredType converterType,
            boolean isSupplier) {
        if (converterType.getTypeArguments().size() != 1) {
            return left(match.fail(converterRawType(converterType)));
        }
        TypeMirror typeArgument = converterType.getTypeArguments().get(0);
        if (tool.isList(typeArgument)) {
            return left(match.fail("invalid converter class: converter must not return list"));
        }
        if (tool.isOptionalish(typeArgument)) {
            return left(match.fail("invalid converter class: converter must not return optional"));
        }
        if (tool.isSameType(typeArgument, Boolean.TYPE)) {
            // impossible in current java, but who knows
            return left(match.fail("invalid converter class: converter must not return boolean"));
        }
        return right(mappingFactoryFactory.create(converter, typeArgument, match, isSupplier));
    }

    private <M extends Item>
    Optional<Either<ValidationFailure, MappingFactory<M>>> checkSuppliedConverter(
            TypeElement converter, Match<M> match) {
        return converter.getInterfaces().stream()
                .filter(inter -> tool.isSameErasure(inter, Supplier.class))
                .map(AS_DECLARED::visit)
                .flatMap(Optional::stream)
                .findFirst()
                .map(declaredType -> checkSuppliedConverter(converter, match, declaredType));
    }

    private <M extends Item>
    Either<ValidationFailure, MappingFactory<M>> checkSuppliedConverter(
            TypeElement converter,
            Match<M> match,
            DeclaredType supplierType) {
        if (supplierType.getTypeArguments().size() != 1) {
            return left(match.fail(converterRawType(supplierType)));
        }
        return AS_DECLARED.visit(supplierType.getTypeArguments().get(0))
                .filter(typeArgument -> tool.isSameErasure(typeArgument, StringConverter.class))
                .<Either<ValidationFailure, DeclaredType>>map(Either::right)
                .orElseGet(() -> left(match.fail(errorConverterType())))
                .flatMap(suppliedType -> handleConverter(converter, match, suppliedType, true));
    }

    private <M extends Item>
    Optional<Either<ValidationFailure, MappingFactory<M>>> checkDirectConverter(
            TypeElement converter, Match<M> match) {
        return Optional.of(converter.getSuperclass())
                .filter(inter -> tool.isSameErasure(inter, StringConverter.class))
                .flatMap(AS_DECLARED::visit)
                .map(converterType ->
                        handleConverter(converter, match, converterType, false));
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
