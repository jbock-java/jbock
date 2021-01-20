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
        .choose(typeArguments -> handleSupplier(typeArguments).mapLeft(Optional::of))
        .chooseLeft(message -> message.<Either<String, FunctionType>>map(Either::left)
            .orElseGet(this::handleNotSupplier));
  }

  private Either<String, FunctionType> handleNotSupplier() {
    return checkImplements(Function.class.getCanonicalName())
        .map(expected -> new FunctionType(expected, false))
        .mapLeft(message -> message.orElse("not a " + Function.class.getCanonicalName() +
            " or " + Supplier.class.getCanonicalName() +
            "<" + Function.class.getCanonicalName() + ">"));
  }

  private Either<String, FunctionType> handleSupplier(List<? extends TypeMirror> typeArguments) {
    TypeMirror typearg = typeArguments.get(0);
    if (typearg.getKind() != TypeKind.DECLARED) {
      return left("not a " + Function.class.getCanonicalName() + " or " + Supplier.class.getCanonicalName() +
          "<" + Function.class.getCanonicalName() + ">");
    }
    DeclaredType actual = asDeclared(typearg);
    if (!tool.isSameErasure(actual, Function.class.getCanonicalName())) {
      return left("expected " + Function.class.getCanonicalName() + " but found " + actual);
    }
    if (actual.getTypeArguments().size() != tool.asTypeElement(Function.class.getCanonicalName()).getTypeParameters().size()) {
      return left("raw type: " + actual);
    }
    return right(new FunctionType(actual.getTypeArguments(), true));
  }

  private Either<Optional<String>, List<? extends TypeMirror>> checkImplements(String candidate) {
    return Either.<Optional<String>, DeclaredType>fromOptional(
        Optional.empty(),
        mapperClass.getInterfaces().stream()
            .filter(inter -> tool.isSameErasure(inter, candidate))
            .map(TypeTool::asDeclared)
            .findFirst())
        .choose(declared -> {
          List<? extends TypeMirror> typeArguments = declared.getTypeArguments();
          List<? extends TypeParameterElement> typeParams = tool.asTypeElement(candidate).getTypeParameters();
          if (typeArguments.size() != typeParams.size()) {
            return left(Optional.of("raw type: " + declared));
          }
          return right(typeArguments);
        });
  }
}
