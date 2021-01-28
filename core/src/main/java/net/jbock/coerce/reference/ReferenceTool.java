package net.jbock.coerce.reference;

import net.jbock.compiler.TypeTool;
import net.jbock.either.Either;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.jbock.compiler.TypeTool.AS_DECLARED;
import static net.jbock.either.Either.fromSuccess;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

public class ReferenceTool {

  private final TypeElement mapperClass;
  private final TypeTool tool;

  @Inject
  ReferenceTool(TypeTool tool, TypeElement mapperClass) {
    this.tool = tool;
    this.mapperClass = mapperClass;
  }

  public Either<String, FunctionType> getReferencedType() {
    Optional<DeclaredType> implementsSupplier = checkImplements(Supplier.class);
    Optional<DeclaredType> implementsFunction = checkImplements(Function.class);
    if (implementsSupplier.isPresent() && implementsFunction.isPresent()) {
      return left(mapperNotFunction() + " but not both");
    }
    if (!implementsSupplier.isPresent() && !implementsFunction.isPresent()) {
      return left(mapperNotFunction());
    }
    if (implementsSupplier.isPresent()) {
      return fromSuccess("", implementsSupplier)
          .filter(typeArguments -> checkNotRaw(Supplier.class, typeArguments))
          .flatMap(this::handleSupplier);
    }
    return fromSuccess("", implementsFunction)
        .filter(typeArguments -> checkNotRaw(Function.class, typeArguments))
        .map(DeclaredType::getTypeArguments)
        .map(typeArguments -> new FunctionType(typeArguments, false));
  }

  private Either<String, FunctionType> handleSupplier(DeclaredType declaredType) {
    TypeMirror typearg = declaredType.getTypeArguments().get(0);
    if (typearg.getKind() != TypeKind.DECLARED) {
      return left(mapperNotFunction());
    }
    DeclaredType suppliedFunction = AS_DECLARED.visit(typearg);
    if (!tool.isSameErasure(suppliedFunction, Function.class.getCanonicalName())) {
      return left(mapperNotFunction());
    }
    if (suppliedFunction.getTypeArguments().size() != 2) {
      return left(mapperRawType());
    }
    return right(new FunctionType(suppliedFunction.getTypeArguments(), true));
  }

  private Optional<DeclaredType> checkImplements(Class<?> candidate) {
    return mapperClass.getInterfaces().stream()
        .filter(inter -> tool.isSameErasure(inter, candidate.getCanonicalName()))
        .map(AS_DECLARED::visit)
        .findFirst();
  }

  private Optional<String> checkNotRaw(Class<?> candidate, DeclaredType declaredType) {
    List<? extends TypeParameterElement> typeParams = tool.asTypeElement(candidate.getCanonicalName()).getTypeParameters();
    if (declaredType.getTypeArguments().size() != typeParams.size()) {
      return Optional.of(mapperRawType());
    }
    return Optional.empty();
  }

  private String mapperNotFunction() {
    return "mapper should implement " + Function.class.getSimpleName() +
        "<String, ?> or " + Supplier.class.getSimpleName() +
        "<" + Function.class.getSimpleName() + "<String, ?>>";
  }

  private String mapperRawType() {
    return "raw type in mapper class";
  }
}
