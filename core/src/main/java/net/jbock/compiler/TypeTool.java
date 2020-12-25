package net.jbock.compiler;

import net.jbock.coerce.either.Either;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleElementVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.lang.model.util.Types;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static net.jbock.coerce.either.Either.left;
import static net.jbock.coerce.either.Either.right;

public class TypeTool {

  private static final TypeVisitor<List<? extends TypeMirror>, Void> TYPEARGS =
      new SimpleTypeVisitor8<List<? extends TypeMirror>, Void>() {
        @Override
        public List<? extends TypeMirror> visitDeclared(DeclaredType declaredType, Void _null) {
          return declaredType.getTypeArguments();
        }

        @Override
        protected List<? extends TypeMirror> defaultAction(TypeMirror e, Void _null) {
          return Collections.emptyList();
        }
      };

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

  private final Types types;

  private final Elements elements;

  // visible for testing
  public TypeTool(Elements elements, Types types) {
    this.types = types;
    this.elements = elements;
  }

  /**
   * @return {@code true} means failure
   */
  private String unify(TypeMirror x, TypeMirror y, Map<String, TypeMirror> acc) {
    if (y.getKind() == TypeKind.TYPEVAR) {
      acc.put(y.toString(), x);
      return null; // success
    }
    if (x.getKind() == TypeKind.TYPEVAR) {
      return "can't unify " + y + " with typevar " + x;
    }
    if (x.getKind() == TypeKind.DECLARED) {
      DeclaredType xx = asDeclared(x);
      if (xx.getTypeArguments().isEmpty()) {
        if (!isAssignable(y, x)) {
          return "Unification failed: can't assign " + y + " to " + x;
        }
      } else {
        if (!isSameErasure(x, y)) {
          return "Unification failed: " + y + " and " + x + " have different erasure";
        }
      }
    } else {
      if (!isSameErasure(x, y)) {
        return "Unification failed: " + y + " and " + x + " have different erasure";
      }
    }
    if (isRaw(x)) {
      return "raw type: " + x;
    }
    if (isRaw(y)) {
      return "raw type: " + y;
    }
    List<? extends TypeMirror> xargs = x.accept(TYPEARGS, null);
    List<? extends TypeMirror> yargs = y.accept(TYPEARGS, null);
    for (int i = 0; i < yargs.size(); i++) {
      String failure = unify(xargs.get(i), yargs.get(i), acc);
      if (failure != null) {
        return failure;
      }
    }
    return null; // success
  }

  public Either<String, TypevarMapping> unify(TypeMirror concreteType, TypeMirror ym, Function<String, ValidationException> errorHandler) {
    Map<String, TypeMirror> acc = new LinkedHashMap<>();
    String failure = unify(concreteType, ym, acc);
    return failure != null ? left(failure) : right(new TypevarMapping(acc, this, errorHandler));
  }

  public boolean isRaw(TypeMirror m) {
    if (m.getKind() != TypeKind.DECLARED) {
      return false;
    }
    DeclaredType declaredType = asDeclared(m);
    return declaredType.getTypeArguments().isEmpty() && !asTypeElement(m).getTypeParameters().isEmpty();
  }

  public DeclaredType getDeclaredType(Class<?> clazz, List<? extends TypeMirror> typeArguments) {
    return getDeclaredType(asTypeElement(clazz), typeArguments.toArray(new TypeMirror[0]));
  }

  public DeclaredType getDeclaredType(TypeElement element, TypeMirror[] typeArguments) {
    return types.getDeclaredType(element, typeArguments);
  }

  public boolean isSameType(TypeMirror mirror, Class<?> test) {
    return types.isSameType(mirror, asTypeElement(test).asType());
  }

  public Optional<TypeMirror> unwrap(Class<?> wrapper, TypeMirror mirror) {
    if (!isSameErasure(mirror, wrapper)) {
      return Optional.empty();
    }
    DeclaredType declaredType = asDeclared(mirror);
    if (declaredType.getTypeArguments().isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(declaredType.getTypeArguments().get(0));
  }

  public boolean isSameType(TypeMirror mirror, TypeMirror test) {
    return types.isSameType(mirror, test);
  }

  PrimitiveType getPrimitiveBoolean() {
    return types.getPrimitiveType(TypeKind.BOOLEAN);
  }

  public boolean isAssignable(TypeMirror x, TypeMirror y) {
    return types.isAssignable(x, y);
  }

  public boolean isSameErasure(TypeMirror x, Class<?> y) {
    return isSameErasure(x, asType(y));
  }

  public boolean isSameErasure(TypeMirror x, TypeMirror y) {
    return types.isSameType(types.erasure(x), types.erasure(y));
  }

  public TypeMirror erasure(TypeMirror typeMirror) {
    return types.erasure(typeMirror);
  }

  public TypeMirror asType(Class<?> type) {
    return elements.getTypeElement(type.getCanonicalName()).asType();
  }

  public DeclaredType optionalOf(Class<?> type) {
    return optionalOf(asTypeElement(type).asType());
  }

  private DeclaredType optionalOf(TypeMirror typeMirror) {
    return types.getDeclaredType(asTypeElement(Optional.class), typeMirror);
  }

  public boolean isEnumType(TypeMirror mirror) {
    return types.directSupertypes(mirror).stream()
        .anyMatch(t -> isSameErasure(types.directSupertypes(mirror).get(0), Enum.class));
  }

  public boolean isReachable(TypeMirror mirror) {
    TypeKind kind = mirror.getKind();
    if (kind != TypeKind.DECLARED) {
      return true;
    }
    DeclaredType declared = asDeclared(mirror);
    if (declared.asElement().getModifiers().contains(Modifier.PRIVATE)) {
      return false;
    }
    List<? extends TypeMirror> typeArguments = declared.getTypeArguments();
    for (TypeMirror typeArgument : typeArguments) {
      if (!isReachable(typeArgument)) {
        return false;
      }
    }
    return true;
  }

  public TypeMirror box(TypeMirror mirror) {
    PrimitiveType primitive = mirror.accept(AS_PRIMITIVE, null);
    return primitive == null ? mirror : types.boxedClass(primitive).asType();
  }

  private TypeElement asTypeElement(Class<?> clazz) {
    return elements.getTypeElement(clazz.getCanonicalName());
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

  public boolean isOutOfBounds(TypeMirror mirror, List<? extends TypeMirror> bounds) {
    return bounds.stream().anyMatch(bound -> !types.isAssignable(mirror, bound));
  }

  public Either<String, TypeMirror> getBound(TypeParameterElement p) {
    List<? extends TypeMirror> bounds = p.getBounds();
    if (bounds.isEmpty()) {
      return right(getDeclaredType(Object.class, Collections.emptyList()));
    }
    if (bounds.size() >= 2) {
      return left("Intersection type is not supported for typevar " + p.toString());
    }
    return right(bounds.get(0));
  }

  public Either<Function<String, String>, TypeMirror> getSpecialization(TypeMirror thisType, TypeMirror thatType) {
    if (isAssignable(thisType, thatType)) {
      return right(thisType);
    }
    if (isAssignable(thatType, thisType)) {
      return right(thatType);
    }
    return left(key -> String.format("Cannot infer %s: %s vs %s", key, thisType, thatType));
  }

  public Optional<DeclaredType> checkImplements(TypeElement dog, Class<?> animal) {
    for (TypeMirror inter : dog.getInterfaces()) {
      if (isSameErasure(inter, animal)) {
        return Optional.of(TypeTool.asDeclared(inter));
      }
    }
    return Optional.empty();
  }
}
