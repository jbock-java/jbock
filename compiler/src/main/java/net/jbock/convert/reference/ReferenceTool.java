package net.jbock.convert.reference;

import net.jbock.common.TypeTool;
import net.jbock.convert.ParameterScope;
import net.jbock.either.Either;
import net.jbock.util.StringConverter;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;
import static net.jbock.either.Either.unbalancedRight;

@ParameterScope
public class ReferenceTool {

  private final TypeTool tool;

  @Inject
  ReferenceTool(TypeTool tool) {
    this.tool = tool;
  }

  public Either<String, StringConverterType> getReferencedType(TypeElement converter) {
    Optional<DeclaredType> supplier = checkSupplier(converter);
    Optional<DeclaredType> stringConverter = checkStringConverter(converter);
    if (supplier.isPresent() && stringConverter.isPresent()) {
      return left(errorConverterType() + " but not both");
    }
    return supplier.map(this::handleSupplier)
        .or(() -> stringConverter.map(c -> handleStringConverter(c, false)))
        .orElseGet(() -> left(errorConverterType()));
  }

  private Either<String, StringConverterType> handleSupplier(DeclaredType declaredType) {
    if (declaredType.getTypeArguments().size() != 1) {
      return left(converterRawType());
    }
    TypeMirror typeArgument = declaredType.getTypeArguments().get(0);
    return unbalancedRight(AS_DECLARED.visit(typeArgument)
        .filter(suppliedFunction -> tool.isSameErasure(suppliedFunction,
            StringConverter.class.getCanonicalName())))
        .orElseLeft(this::errorConverterType)
        .flatMap(suppliedType -> handleStringConverter(suppliedType, true));
  }

  private Either<String, StringConverterType> handleStringConverter(
      DeclaredType stringConverter, boolean isSupplier) {
    if (stringConverter.getTypeArguments().size() != 1) {
      return left(converterRawType());
    }
    TypeMirror typeArgument = stringConverter.getTypeArguments().get(0);
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
    return "converter must extend " + StringConverter.class.getSimpleName() +
        "<X> or implement " + Supplier.class.getSimpleName() +
        "<" + StringConverter.class.getSimpleName() + "<X>>";
  }

  private String converterRawType() {
    return "raw type in converter class";
  }
}
