package net.jbock.compiler;

import net.jbock.coerce.TypevarMapping;
import net.jbock.coerce.Unifier;
import net.jbock.coerce.either.Either;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleElementVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.lang.model.util.Types;
import java.util.Optional;
import java.util.function.Function;

import static net.jbock.coerce.either.Either.left;
import static net.jbock.coerce.either.Either.right;

public class TypeTool {

  public static final TypeVisitor<DeclaredType, Void> AS_DECLARED =
      new SimpleTypeVisitor8<DeclaredType, Void>() {
        @Override
        public DeclaredType visitDeclared(DeclaredType declaredType, Void _null) {
          return declaredType;
        }
      };

  public static final TypeVisitor<PrimitiveType, Void> AS_PRIMITIVE =
      new SimpleTypeVisitor8<PrimitiveType, Void>() {
        @Override
        public PrimitiveType visitPrimitive(PrimitiveType primitiveType, Void _null) {
          return primitiveType;
        }
      };

  public static final ElementVisitor<TypeElement, Void> AS_TYPE_ELEMENT =
      new SimpleElementVisitor8<TypeElement, Void>() {
        @Override
        public TypeElement visitType(TypeElement typeElement, Void _null) {
          return typeElement;
        }
      };

  public static final TypeVisitor<TypeVariable, Void> AS_TYPEVAR =
      new SimpleTypeVisitor8<TypeVariable, Void>() {

        @Override
        public TypeVariable visitTypeVariable(TypeVariable t, Void unused) {
          return t;
        }
      };

  public static final TypeVisitor<IntersectionType, Void> AS_INTERSECTION =
      new SimpleTypeVisitor8<IntersectionType, Void>() {

        @Override
        public IntersectionType visitIntersection(IntersectionType t, Void unused) {
          return t;
        }
      };

  public static final TypeVisitor<ArrayType, Void> AS_ARRAY =
      new SimpleTypeVisitor8<ArrayType, Void>() {
        @Override
        public ArrayType visitArray(ArrayType t, Void unused) {
          return t;
        }
      };

  private final Types types;

  private final Elements elements;

  // visible for testing
  public TypeTool(Elements elements, Types types) {
    this.types = types;
    this.elements = elements;
  }

  public boolean isSameType(TypeMirror mirror, String canonicalName) {
    return types.isSameType(mirror, asTypeElement(canonicalName).asType());
  }

  /**
   * The canonical name must be a class with exactly one type parameter.
   */
  public Optional<TypeMirror> getSingleTypeArgument(TypeMirror mirror, String canonicalName) {
    if (!isSameErasure(mirror, canonicalName)) {
      return Optional.empty();
    }
    DeclaredType declaredType = asDeclared(mirror);
    if (declaredType.getTypeArguments().isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(declaredType.getTypeArguments().get(0));
  }

  public boolean isSameErasure(TypeMirror x, String y) {
    TypeElement el = elements.getTypeElement(y);
    return types.isSameType(types.erasure(x), types.erasure(el.asType()));
  }

  public TypeElement asTypeElement(String canonicalName) {
    return elements.getTypeElement(canonicalName);
  }

  public TypeElement asTypeElement(TypeMirror mirror) {
    Element element = types.asElement(mirror);
    if (element == null) {
      throw new IllegalArgumentException("not an element: " + mirror);
    }
    return asTypeElement(element);
  }

  public static TypeElement asTypeElement(Element element) {
    TypeElement result = element.accept(AS_TYPE_ELEMENT, null);
    if (result == null) {
      throw new IllegalArgumentException("not a type element: " + element);
    }
    return result;
  }

  public static DeclaredType asDeclared(TypeMirror mirror) {
    DeclaredType result = mirror.accept(AS_DECLARED, null);
    if (result == null) {
      throw new IllegalArgumentException("not declared: " + mirror);
    }
    return result;
  }

  public TypeMirror getArrayType(TypeMirror componentType) {
    return types.getArrayType(componentType);
  }

  public Either<String, TypevarMapping> unify(
      TypeMirror concreteType,
      TypeMirror ym,
      Function<String, ValidationException> errorHandler) {
    Unifier unifier = new Unifier(types);
    String failure = unifier.unify(concreteType, ym);
    return failure != null ?
        left("Unification failed: " + failure) :
        right(new TypevarMapping(unifier.getResult(), this, errorHandler));
  }

  public Types types() {
    return types;
  }
}
