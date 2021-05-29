package net.jbock.common;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleElementVisitor9;
import javax.lang.model.util.SimpleTypeVisitor9;
import javax.lang.model.util.Types;
import java.util.Optional;

public class TypeTool {

  public static final TypeVisitor<Optional<DeclaredType>, Void> AS_DECLARED =
      new SimpleTypeVisitor9<>() {
        @Override
        public Optional<DeclaredType> visitDeclared(DeclaredType declaredType, Void nothing) {
          return Optional.of(declaredType);
        }

        @Override
        protected Optional<DeclaredType> defaultAction(TypeMirror e, Void unused) {
          return Optional.empty();
        }
      };

  public static final TypeVisitor<Optional<PrimitiveType>, Void> AS_PRIMITIVE =
      new SimpleTypeVisitor9<>() {
        @Override
        public Optional<PrimitiveType> visitPrimitive(PrimitiveType primitiveType, Void nothing) {
          return Optional.of(primitiveType);
        }

        @Override
        protected Optional<PrimitiveType> defaultAction(TypeMirror e, Void unused) {
          return Optional.empty();
        }
      };

  public static final ElementVisitor<Optional<TypeElement>, Void> AS_TYPE_ELEMENT =
      new SimpleElementVisitor9<>() {
        @Override
        public Optional<TypeElement> visitType(TypeElement typeElement, Void nothing) {
          return Optional.of(typeElement);
        }

        @Override
        protected Optional<TypeElement> defaultAction(Element e, Void unused) {
          return Optional.empty();
        }
      };

  private final Types types;

  private final SafeElements elements;

  // visible for testing
  public TypeTool(SafeElements elements, Types types) {
    this.types = types;
    this.elements = elements;
  }

  public boolean isSameType(TypeMirror mirror, Class<?> cl) {
    return isSameType(mirror, cl.getCanonicalName());
  }

  public boolean isSameType(TypeMirror mirror, String canonicalName) {
    return elements.getTypeElement(canonicalName)
        .map(TypeElement::asType)
        .map(type -> types.isSameType(mirror, type))
        .orElse(false);
  }

  public boolean isSameType(TypeMirror mirror, TypeMirror otherType) {
    return types.isSameType(mirror, otherType);
  }

  /**
   * The class must be a class with exactly one type parameter.
   */
  public Optional<TypeMirror> getSingleTypeArgument(
      TypeMirror mirror, Class<?> someClass) {
    if (mirror.getKind() != TypeKind.DECLARED) {
      return Optional.empty();
    }
    String canonicalName = someClass.getCanonicalName();
    return AS_DECLARED.visit(mirror).flatMap(declaredType -> {
      if (declaredType.getTypeArguments().isEmpty()) {
        return Optional.empty();
      }
      if (!isSameErasure(mirror, canonicalName)) {
        return Optional.empty();
      }
      return Optional.of(declaredType.getTypeArguments().get(0));
    });
  }

  public boolean isSameErasure(TypeMirror x, String y) {
    return elements.getTypeElement(y)
        .map(TypeElement::asType)
        .map(type -> types.isSameType(types.erasure(x), types.erasure(type)))
        .orElse(false);
  }
}
