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
import java.util.function.Function;
import java.util.function.Supplier;

import static net.jbock.compiler.TypeTool.asDeclared;
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
    return checkImplements(Supplier.class.getCanonicalName())
        .chooseRight(typeArguments -> handleSupplier(typeArguments).mapLeft(Either::left))
        .chooseLeft(stringOrVoid -> stringOrVoid.chooseRight(v -> handleNotSupplier()));
  }

  private Either<String, FunctionType> handleNotSupplier() {
    return checkImplements(Function.class.getCanonicalName())
        .map(expected -> new FunctionType(expected, false))
        .mapLeft(message -> message.swap().orElse(this::mapperNotFunction));
  }

  private Either<String, FunctionType> handleSupplier(List<? extends TypeMirror> typeArguments) {
    TypeMirror typearg = typeArguments.get(0);
    if (typearg.getKind() != TypeKind.DECLARED) {
      return left(mapperNotFunction());
    }
    DeclaredType suppliedFunction = asDeclared(typearg);
    if (!tool.isSameErasure(suppliedFunction, Function.class.getCanonicalName())) {
      return left(mapperNotFunction());
    }
    if (suppliedFunction.getTypeArguments().size() != 2) {
      return left(mapperRawType());
    }
    return right(new FunctionType(suppliedFunction.getTypeArguments(), true));
  }

  private Either<Either<String, Void>, List<? extends TypeMirror>> checkImplements(String candidate) {
    return Either.<Either<String, Void>, DeclaredType>fromOptional(
        right(),
        mapperClass.getInterfaces().stream()
            .filter(inter -> tool.isSameErasure(inter, candidate))
            .map(TypeTool::asDeclared)
            .findFirst())
        .chooseRight(declared -> {
          List<? extends TypeMirror> typeArguments = declared.getTypeArguments();
          List<? extends TypeParameterElement> typeParams = tool.asTypeElement(candidate).getTypeParameters();
          if (typeArguments.size() != typeParams.size()) {
            return left(left(mapperRawType()));
          }
          return right(typeArguments);
        });
  }

  private String mapperNotFunction() {
    return "expecting mapper of type " + Function.class.getSimpleName() +
        " or " + Supplier.class.getSimpleName() +
        "<" + Function.class.getSimpleName() + ">";
  }

  private String mapperRawType() {
    return "raw type in mapper class";
  }
}
