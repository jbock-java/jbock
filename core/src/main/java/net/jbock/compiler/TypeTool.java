package net.jbock.compiler;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
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
  private boolean unify(TypeMirror x, TypeMirror y, Map<String, TypeMirror> acc) {
    if (x.getKind() == TypeKind.TYPEVAR) {
      return true; // only y can have typevars
    }
    if (y.getKind() == TypeKind.TYPEVAR) {
      acc.put(y.toString(), x);
      return false;
    }
    if (!isSameErasure(x, y)) {
      return true;
    }
    List<? extends TypeMirror> xargs = typeargs(x);
    List<? extends TypeMirror> yargs = typeargs(y);
    if (xargs.size() != yargs.size()) {
      return true;
    }
    for (int i = 0; i < yargs.size(); i++) {
      boolean failure = unify(xargs.get(i), yargs.get(i), acc);
      if (failure) {
        return true;
      }
    }
    return false;
  }

  public Optional<Map<String, TypeMirror>> unify(TypeMirror concreteType, TypeMirror ym) {
    Map<String, TypeMirror> acc = new LinkedHashMap<>();
    boolean failure = unify(concreteType, ym, acc);
    return failure ? Optional.empty() : Optional.of(acc);
  }

  /**
   * @param input    a type
   * @param solution for solving typevars in the input
   * @return the input type, with all typevars resolved. Wildcards remain unchanged.
   */
  public Optional<? extends TypeMirror> substitute(TypeMirror input, Map<String, TypeMirror> solution) {
    if (input.getKind() == TypeKind.TYPEVAR) {
      return Optional.ofNullable(solution.get(input.toString()));
    }
    return substitute(input.accept(AS_DECLARED, null), solution);
  }

  public Optional<DeclaredType> substitute(DeclaredType declaredType, Map<String, TypeMirror> solution) {
    DeclaredType result = subst(declaredType, solution);
    if (result == null) {
      return Optional.empty(); // invalid
    }
    return Optional.of(result);
  }

  private DeclaredType subst(DeclaredType input, Map<String, TypeMirror> solution) {
    List<? extends TypeMirror> typeArguments = input.getTypeArguments();
    TypeMirror[] result = new TypeMirror[typeArguments.size()];
    for (int i = 0; i < typeArguments.size(); i++) {
      TypeMirror typeArgument = typeArguments.get(i);
      TypeMirror opt = null;
      TypeKind kind = typeArgument.getKind();
      if (kind == TypeKind.WILDCARD) {
        opt = typeArgument; // these can stay
      } else if (typeArgument.getKind() == TypeKind.TYPEVAR) {
        opt = solution.getOrDefault(typeArgument.toString(), typeArgument);
      } else if (kind == TypeKind.DECLARED) {
        opt = subst(typeArgument.accept(AS_DECLARED, null), solution);
      } else if (kind == TypeKind.ARRAY) {
        opt = typeArgument;
      }
      if (opt == null) {
        return null;  // error
      }
      result[i] = opt;
    }
    return types.getDeclaredType(input.asElement()
        .accept(AS_TYPE_ELEMENT, null), result);
  }

  public DeclaredType getDeclaredType(Class<?> clasz, List<? extends TypeMirror> typeArguments) {
    return types.getDeclaredType(asDeclared(asType(clasz)).asElement()
        .accept(AS_TYPE_ELEMENT, null), typeArguments.toArray(new TypeMirror[0]));
  }

  public boolean isSameType(TypeMirror mirror, Class<?> test) {
    return types.isSameType(mirror, asTypeElement(test).asType());
  }

  public Optional<TypeMirror> unwrap(Class<?> wrapper, TypeMirror mirror) {
    if (mirror.getKind() != TypeKind.DECLARED) {
      return Optional.empty();
    }
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

  public boolean isRawType(TypeMirror mirror) {
    return types.isSameType(mirror, types.erasure(mirror));
  }

  PrimitiveType getPrimitiveType(TypeKind kind) {
    return types.getPrimitiveType(kind);
  }

  private boolean isSameErasure(TypeMirror x, TypeMirror y) {
    return types.isSameType(types.erasure(x), types.erasure(y));
  }

  public boolean isSameErasure(TypeMirror x, Class<?> y) {
    return isSameErasure(x, erasure(y));
  }

  public TypeMirror erasure(TypeMirror typeMirror) {
    return types.erasure(typeMirror);
  }

  public TypeMirror erasure(TypeElement typeMirror) {
    return types.erasure(typeMirror.asType());
  }

  public TypeMirror erasure(Class<?> type) {
    return erasure(asType(type));
  }

  public TypeMirror asType(Class<?> type) {
    return elements.getTypeElement(type.getCanonicalName()).asType();
  }

  public TypeMirror optionalOf(Class<?> type) {
    return optionalOf(asTypeElement(type).asType());
  }

  private TypeMirror optionalOf(TypeMirror typeMirror) {
    return types.getDeclaredType(
        asTypeElement(Optional.class),
        typeMirror);
  }

  public List<? extends TypeMirror> getDirectSupertypes(TypeMirror mirror) {
    return types.directSupertypes(mirror);
  }

  public boolean isPrivateType(TypeMirror mirror) {
    Element element = types.asElement(mirror);
    if (element == null) {
      return false;
    }
    return element.getModifiers().contains(Modifier.PRIVATE);
  }

  public TypeMirror box(TypeMirror mirror) {
    PrimitiveType primitive = mirror.accept(AS_PRIMITIVE, null);
    if (primitive == null) {
      return mirror;
    }
    return types.boxedClass(primitive).asType();
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
    for (TypeMirror bound : bounds) {
      if (!types.isAssignable(mirror, bound)) {
        return true;
      }
    }
    return false;
  }

  private List<? extends TypeMirror> typeargs(TypeMirror mirror) {
    return mirror.accept(TYPEARGS, null);
  }
}
