package net.jbock.compiler;

import dagger.Reusable;

import javax.inject.Inject;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleElementVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.lang.model.util.Types;
import java.util.Optional;

@Reusable
public class TypeTool {

  public static final TypeVisitor<DeclaredType, Void> AS_DECLARED =
      new SimpleTypeVisitor8<>() {
        @Override
        public DeclaredType visitDeclared(DeclaredType declaredType, Void nothing) {
          return declaredType;
        }
      };

  public static final TypeVisitor<PrimitiveType, Void> AS_PRIMITIVE =
      new SimpleTypeVisitor8<>() {
        @Override
        public PrimitiveType visitPrimitive(PrimitiveType primitiveType, Void nothing) {
          return primitiveType;
        }
      };

  public static final ElementVisitor<TypeElement, Void> AS_TYPE_ELEMENT =
      new SimpleElementVisitor8<>() {
        @Override
        public TypeElement visitType(TypeElement typeElement, Void nothing) {
          return typeElement;
        }
      };

  private final Types types;

  private final Elements elements;

  // visible for testing
  @Inject
  public TypeTool(Elements elements, Types types) {
    this.types = types;
    this.elements = elements;
  }

  public boolean isSameType(TypeMirror mirror, String canonicalName) {
    return types.isSameType(mirror, asTypeElement(canonicalName).asType());
  }

  public boolean isSameType(TypeMirror mirror, TypeMirror otherType) {
    return types.isSameType(mirror, otherType);
  }

  /**
   * The canonical name must be a class with exactly one type parameter.
   */
  public Optional<TypeMirror> getSingleTypeArgument(TypeMirror mirror, Class<?> someClass) {
    String canonicalName = someClass.getCanonicalName();
    if (!isSameErasure(mirror, canonicalName)) {
      return Optional.empty();
    }
    DeclaredType declaredType = AS_DECLARED.visit(mirror);
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

  public Types types() {
    return types;
  }

  public Elements elements() {
    return elements;
  }
}
