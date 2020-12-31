package net.jbock.compiler;

import net.jbock.coerce.TypevarMapping;
import net.jbock.coerce.either.Either;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleElementVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.lang.model.util.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

  private static final TypeVisitor<PrimitiveType, Void> AS_PRIMITIVE =
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

  // solve typevars in y
  private String unify(TypeMirror x, TypeMirror y, Map<String, TypeMirror> acc) {
    if (y.getKind() == TypeKind.TYPEVAR) {
      if (!types.isAssignable(x, y.accept(AS_TYPEVAR, null).getUpperBound())) {
        return "Unification failed: can't assign " + x + " to " + y;
      }
      acc.put(y.toString(), x);
      return null; // success
    }
    if (x.getKind() == TypeKind.TYPEVAR) {
      if (!types.isAssignable(y, x.accept(AS_TYPEVAR, null).getUpperBound())) {
        return "Unification failed: can't assign " + y + " to " + x;
      }
      return null; // no constraint for y
    }
    if (x.getKind() == TypeKind.WILDCARD || y.getKind() == TypeKind.WILDCARD) {
      return "Unification failed: wildcard is not allowed here";
    }
    if (x.getKind() != y.getKind()) {
      return "can't unify " + x + " with " + y;
    }
    if (x.getKind() == TypeKind.ARRAY) {
      TypeMirror xc = x.accept(AS_ARRAY, null).getComponentType();
      TypeMirror yc = y.accept(AS_ARRAY, null).getComponentType();
      return unify(xc, yc, acc);
    }
    if (x.getKind() != TypeKind.DECLARED) {
      if (!types.isAssignable(y, x)) {
        return "Unification failed: can't assign " + y + " to " + x;
      }
      return null;
    }
    DeclaredType xx = asDeclared(x);
    DeclaredType yy = asDeclared(y);
    List<? extends TypeMirror> xargs = xx.getTypeArguments();
    if (xargs.isEmpty()) {
      if (!types.isAssignable(y, x)) {
        return "Unification failed: can't assign " + y + " to " + x;
      }
    }
    if (!isSameErasure(x, y)) {
      return "Unification failed: " + y + " and " + x + " have different erasure";
    }
    List<? extends TypeMirror> yargs = yy.getTypeArguments();
    if (xargs.size() != yargs.size()) {
      return "can't unify " + x + " with " + y;
    }
    for (int i = 0; i < yargs.size(); i++) {
      String failure = unify(xargs.get(i), yargs.get(i), acc);
      if (failure != null) {
        return failure;
      }
    }
    return null;
  }

  public Either<String, TypevarMapping> unify(TypeMirror concreteType, TypeMirror ym, Function<String, ValidationException> errorHandler) {
    Map<String, TypeMirror> acc = new LinkedHashMap<>();
    String failure = unify(concreteType, ym, acc);
    return failure != null ? left(failure) : right(new TypevarMapping(acc, this, errorHandler));
  }

  public DeclaredType getDeclaredType(Class<?> clazz, List<? extends TypeMirror> typeArguments) {
    return getDeclaredType(asTypeElement(clazz.getCanonicalName()), typeArguments.toArray(new TypeMirror[0]));
  }

  public DeclaredType getDeclaredType(TypeElement element, TypeMirror[] typeArguments) {
    return types.getDeclaredType(element, typeArguments);
  }

  public boolean isSameType(TypeMirror mirror, String canonicalName) {
    return types.isSameType(mirror, asTypeElement(canonicalName).asType());
  }

  public Optional<TypeMirror> unwrap(TypeMirror mirror, String canonicalName) {
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
    return isSameErasure(x, elements.getTypeElement(y).asType());
  }

  private boolean isSameErasure(TypeMirror x, TypeMirror y) {
    return types.isSameType(types.erasure(x), types.erasure(y));
  }

  public TypeMirror erasure(TypeMirror typeMirror) {
    return types.erasure(typeMirror);
  }

  public DeclaredType optionalOf(String canonicalName) {
    return optionalOf(asTypeElement(canonicalName).asType());
  }

  private DeclaredType optionalOf(TypeMirror typeMirror) {
    return types.getDeclaredType(asTypeElement(Optional.class.getCanonicalName()), typeMirror);
  }

  public boolean isEnumType(TypeMirror mirror) {
    return types.directSupertypes(mirror).stream()
        .anyMatch(t -> isSameErasure(types.directSupertypes(mirror).get(0), Enum.class.getCanonicalName()));
  }

  public boolean isUnreachable(TypeMirror mirror) {
    TypeKind kind = mirror.getKind();
    if (kind != TypeKind.DECLARED) {
      return false;
    }
    DeclaredType declared = asDeclared(mirror);
    if (declared.asElement().getModifiers().contains(Modifier.PRIVATE)) {
      return true;
    }
    List<? extends TypeMirror> typeArguments = declared.getTypeArguments();
    for (TypeMirror typeArgument : typeArguments) {
      if (isUnreachable(typeArgument)) {
        return true;
      }
    }
    return false;
  }

  public TypeMirror box(TypeMirror mirror) {
    PrimitiveType primitive = mirror.accept(AS_PRIMITIVE, null);
    return primitive == null ? mirror : types.boxedClass(primitive).asType();
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

  public Either<Function<String, String>, TypeMirror> getSpecialization(TypeMirror thisType, TypeMirror thatType) {
    if (types.isAssignable(thisType, thatType)) {
      return right(thisType);
    }
    if (types.isAssignable(thatType, thisType)) {
      return right(thatType);
    }
    return left(key -> String.format("Cannot infer %s: %s vs %s", key, thisType, thatType));
  }

  public TypeMirror getArrayType(TypeMirror componentType) {
    return types.getArrayType(componentType);
  }
}
