package net.jbock.convert.reference;

import net.jbock.compiler.TypeTool;
import net.jbock.either.Either;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.jbock.compiler.TypeTool.AS_DECLARED;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.ofRight;
import static net.jbock.either.Either.right;

public class ReferenceTool {

  private final TypeTool tool;

  @Inject
  ReferenceTool(TypeTool tool) {
    this.tool = tool;
  }

  public Either<String, FunctionType> getReferencedType(TypeElement converter) {
    Optional<DeclaredType> implementsSupplier = checkImplements(Supplier.class, converter);
    Optional<DeclaredType> implementsFunction = checkImplements(Function.class, converter);
    if (implementsSupplier.isPresent() && implementsFunction.isPresent()) {
      return left(converteNotFunction() + " but not both");
    }
    if (!implementsSupplier.isPresent() && !implementsFunction.isPresent()) {
      return left(converteNotFunction());
    }
    if (implementsSupplier.isPresent()) {
      return ofRight(implementsSupplier).orLeft("")
          .flatMap(this::handleSupplier);
    }
    return ofRight(implementsFunction).orLeft("")
        .flatMap(declaredType -> handleFunction(declaredType, false));
  }

  private Either<String, FunctionType> handleSupplier(DeclaredType declaredType) {
    if (declaredType.getTypeArguments().size() != 1) {
      return left(converterRawType());
    }
    TypeMirror typearg = declaredType.getTypeArguments().get(0);
    if (typearg.getKind() != TypeKind.DECLARED) {
      return left(converteNotFunction());
    }
    DeclaredType suppliedFunction = AS_DECLARED.visit(typearg);
    if (!tool.isSameErasure(suppliedFunction, Function.class.getCanonicalName())) {
      return left(converteNotFunction());
    }
    return handleFunction(suppliedFunction, true);
  }

  private Either<String, FunctionType> handleFunction(DeclaredType suppliedFunction, boolean isSupplier) {
    if (suppliedFunction.getTypeArguments().size() != 2) {
      return left(converterRawType());
    }
    TypeMirror inputType = suppliedFunction.getTypeArguments().get(0);
    if (!tool.isSameType(inputType, String.class.getCanonicalName())) {
      return left("converter should implement Function<String, ?>");
    }
    return right(new FunctionType(suppliedFunction.getTypeArguments(), isSupplier));
  }

  private Optional<DeclaredType> checkImplements(Class<?> candidate, TypeElement converter) {
    return converter.getInterfaces().stream()
        .filter(inter -> tool.isSameErasure(inter, candidate.getCanonicalName()))
        .map(AS_DECLARED::visit)
        .findFirst();
  }

  private String converteNotFunction() {
    return "converter should implement " + Function.class.getSimpleName() +
        "<String, ?> or " + Supplier.class.getSimpleName() +
        "<" + Function.class.getSimpleName() + "<String, ?>>";
  }

  private String converterRawType() {
    return "raw type in converter class";
  }
}
