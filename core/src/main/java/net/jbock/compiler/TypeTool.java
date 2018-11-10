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

  private TypeTool(Types types, Elements elements) {
    this.types = types;
    this.elements = elements;
  }

  public static TypeTool get() {
    return instance;
  }

  static void setInstance(Types types, Elements elements) {
    instance = new TypeTool(types, elements);
  }

  static void unset() {
    instance = null;
  }

  private void unify(TypeMirror x, TypeMirror ym, Map<String, TypeMirror> acc, boolean[] failure) {
    if (failure[0]) {
      return;
    }
    if (ym.getKind() == TypeKind.TYPEVAR) {
      acc.put(ym.toString(), x);
      return;
    }
    if (!isSameErasure(x, ym)) {
      failure[0] = true;
      return;
    }
    List<? extends TypeMirror> xargs = typeargs(x);
    List<? extends TypeMirror> yargs = typeargs(ym);
    if (xargs.size() != yargs.size()) {
      failure[0] = true;
      return;
    }
    for (int i = 0; i < yargs.size(); i++) {
      TypeMirror yarg = yargs.get(i);
      TypeMirror xarg = xargs.get(i);
      unify(xarg, yarg, acc, failure);
    }
  }

  public Optional<Map<String, TypeMirror>> unify(TypeMirror concreteType, TypeMirror ym) {
    Map<String, TypeMirror> acc = new HashMap<>();
    boolean[] failure = new boolean[1];
    unify(concreteType, ym, acc, failure);
    if (failure[0]) {
      return Optional.empty();
    }
    return Optional.of(acc);
  }

  public Optional<TypeMirror> substitute(TypeMirror input, Map<String, TypeMirror> solution) {
    return Optional.ofNullable(substitute(input, solution, Collections.emptyList()));
  }

  private TypeMirror substitute(TypeMirror input, Map<String, TypeMirror> solution, List<? extends TypeMirror> bounds) {
    if (input.getKind() == TypeKind.TYPEVAR) {
      TypeMirror value = solution.get(input.toString());
      if (value == null) {
        return null; // invalid solution
      }
      for (TypeMirror bound : bounds) {
        if (!types.isAssignable(value, bound)) {
          return null;
        }
      }
      return value;
    }
    DeclaredType declaredType = input.accept(AS_DECLARED, null);
    if (declaredType == null) {
      return input;
    }
    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
    TypeElement typeElement = declaredType.asElement().accept(AS_TYPE_ELEMENT, null);
    List<? extends TypeParameterElement> typeParameters = typeElement.getTypeParameters();
    TypeMirror[] result = new TypeMirror[typeParameters.size()];
    for (int i = 0; i < typeParameters.size(); i++) {
      TypeParameterElement typeParameter = typeParameters.get(i);
      TypeMirror typeArgument = typeArguments.get(i);
      TypeMirror opt = substitute(typeArgument, solution, typeParameter.getBounds());
      if (opt == null) {
        return null;
      }
      result[i] = opt;
    }
    return types.getDeclaredType(typeElement, result);
  }

  public boolean isSameType(TypeMirror mirror, Class<?> test) {
    return types.isSameType(mirror, getTypeElement(test).asType());
  }

  public boolean isSameType(TypeMirror mirror, TypeMirror test) {
    return types.isSameType(mirror, test);
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
