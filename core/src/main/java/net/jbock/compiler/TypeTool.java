package net.jbock.compiler;

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
import java.util.HashMap;
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

  private static final TypeVisitor<DeclaredType, Void> AS_DECLARED =
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

  private static final ElementVisitor<TypeElement, Void> AS_TYPE_ELEMENT =
      new SimpleElementVisitor8<TypeElement, Void>() {
        @Override
        public TypeElement visitType(TypeElement typeElement, Void _null) {
          return typeElement;
        }
      };

  public List<? extends TypeMirror> typeargs(TypeMirror mirror) {
    return mirror.accept(TYPEARGS, null);
  }

  private final Types types;

  private final Elements elements;

  private static TypeTool instance;

  // visible for testing
  public TypeTool(Elements elements, Types types) {
    this.types = types;
    this.elements = elements;
  }

  public static TypeTool get() {
    return instance;
  }

  static TypeTool init(Elements elements, Types types) {
    instance = new TypeTool(elements, types);
    return instance;
  }

  static void unset() {
    instance = null;
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
    Map<String, TypeMirror> acc = new HashMap<>();
    boolean failure = unify(concreteType, ym, acc);
    return failure ? Optional.empty() : Optional.of(acc);
  }

  /**
   * @param input a type
   * @param solution for solving typevars in the input
   * @return the input type, with all typevars resolved. Wildcards remain unchanged.
   */
  public TypeMirror substitute(TypeMirror input, Map<String, TypeMirror> solution) {
    TypeMirror result = subst(input, solution);
    if (result == null) {
      return null; // invalid
    }
    if (!isAssignableToTypeElement(result)) {
      return null;
    }
    return result;
  }

  private boolean isAssignableToTypeElement(TypeMirror result) {
    if (result.getKind() == TypeKind.WILDCARD ||
        result.getKind() == TypeKind.TYPEVAR) {
      return true;
    }
    DeclaredType declaredType = result.accept(AS_DECLARED, null);
    TypeElement typeElement = declaredType.asElement().accept(AS_TYPE_ELEMENT, null);
    List<? extends TypeParameterElement> typeParameters = typeElement.getTypeParameters();
    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
    if (typeArguments.size() != typeParameters.size()) {
      return false;
    }
    for (int i = 0; i < typeParameters.size(); i++) {
      TypeMirror argument = typeArguments.get(i);
      if (argument.getKind() == TypeKind.WILDCARD) {
        continue;
      }
      for (TypeMirror bound : typeParameters.get(i).getBounds()) {
        if (!types.isAssignable(argument, bound)) {
          return false;
        }
      }
      if (!isAssignableToTypeElement(argument)) {
        return false;
      }
    }
    return true;
  }

  private TypeMirror subst(
      TypeMirror input,
      Map<String, TypeMirror> solution) {
    if (input.getKind() == TypeKind.TYPEVAR) {
      return solution.get(input.toString());
    }
    if (input.getKind() == TypeKind.WILDCARD) {
      return input; // these can stay
    }
    DeclaredType declaredType = input.accept(AS_DECLARED, null);
    if (declaredType == null) {
      return null; // invalid input
    }
    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
    TypeMirror[] result = new TypeMirror[typeArguments.size()];
    for (int i = 0; i < typeArguments.size(); i++) {
      TypeMirror typeArgument = typeArguments.get(i);
      TypeMirror opt = subst(typeArgument, solution);
      if (opt == null) {
        return null; // error
      }
      result[i] = opt;
    }
    return types.getDeclaredType(declaredType.asElement()
        .accept(AS_TYPE_ELEMENT, null), result);
  }

  public boolean isSameType(TypeMirror mirror, Class<?> test) {
    return types.isSameType(mirror, getTypeElement(test).asType());
  }

  public boolean isSameType(TypeMirror mirror, TypeMirror test) {
    return types.isSameType(mirror, test);
  }

  boolean isBooleanPrimitive(TypeMirror mirror) {
    return isSameType(mirror, getPrimitiveType(TypeKind.BOOLEAN));
  }

  public boolean isRawType(TypeMirror mirror) {
    return types.isSameType(mirror, types.erasure(mirror));
  }

  public PrimitiveType getPrimitiveType(TypeKind kind) {
    return types.getPrimitiveType(kind);
  }

  public boolean isSameErasure(TypeMirror x, TypeMirror y) {
    return types.isSameType(types.erasure(x), types.erasure(y));
  }

  public boolean isSameErasure(TypeMirror x, Class<?> y) {
    return isSameErasure(x, erasure(y));
  }

  public TypeMirror erasure(TypeMirror typeMirror) {
    return types.erasure(typeMirror);
  }

  public TypeMirror erasure(Class<?> type) {
    return erasure(asType(type));
  }

  public TypeMirror asType(Class<?> type) {
    return elements.getTypeElement(type.getCanonicalName()).asType();
  }

  public TypeMirror optionalOf(Class<?> type) {
    return types.getDeclaredType(
        getTypeElement(Optional.class),
        getTypeElement(type).asType());
  }

  public TypeMirror listOf(TypeMirror type) {
    return types.getDeclaredType(getTypeElement(List.class), type);
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

  public TypeElement getTypeElement(Class<?> clazz) {
    return elements.getTypeElement(clazz.getCanonicalName());
  }

  TypeElement asTypeElement(TypeMirror mirror) {
    Element element = types.asElement(mirror);
    if (element == null) {
      throw new IllegalArgumentException("no element: " + mirror);
    }
    TypeElement result = element.accept(AS_TYPE_ELEMENT, null);
    if (result == null) {
      throw new IllegalArgumentException("no type element: " + element);
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
}
