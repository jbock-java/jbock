package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleElementVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import static net.jbock.com.squareup.javapoet.WildcardTypeName.subtypeOf;

public final class Util {

  public static final TypeVisitor<DeclaredType, Void> AS_DECLARED =
      new SimpleTypeVisitor8<DeclaredType, Void>() {
        @Override
        public DeclaredType visitDeclared(DeclaredType declaredType, Void _null) {
          return declaredType;
        }
      };

  public static final TypeVisitor<ArrayType, Void> AS_ARRAY =
      new SimpleTypeVisitor8<ArrayType, Void>() {
        @Override
        public ArrayType visitArray(ArrayType t, Void _null) {
          return t;
        }
      };

  public static final ElementVisitor<TypeElement, Void> AS_TYPE_ELEMENT =
      new SimpleElementVisitor8<TypeElement, Void>() {
        @Override
        public TypeElement visitType(TypeElement typeElement, Void _null) {
          return typeElement;
        }
      };

  public static final TypeVisitor<String, Void> QUALIFIED_NAME =
      new SimpleTypeVisitor8<String, Void>() {
        @Override
        public String visitDeclared(DeclaredType declaredType, Void _null) {
          return declaredType.asElement().accept(AS_TYPE_ELEMENT, null).getQualifiedName().toString();
        }

        @Override
        public String visitPrimitive(PrimitiveType t, Void aVoid) {
          return t.toString();
        }

        @Override
        public String visitArray(ArrayType t, Void aVoid) {
          return t.getComponentType().accept(this, null) + "[]";
        }
      };

  public static ParameterizedTypeName optionalOf(TypeName typeName) {
    return ParameterizedTypeName.get(ClassName.get(Optional.class), typeName);
  }

  public static ParameterizedTypeName listOf(TypeName typeName) {
    return ParameterizedTypeName.get(ClassName.get(List.class), typeName);
  }

  static ParameterizedTypeName optionalOfSubtype(TypeName typeName) {
    return ParameterizedTypeName.get(ClassName.get(Optional.class), subtypeOf(typeName));
  }

  static DeclaredType asDeclared(TypeMirror mirror) {
    return mirror.accept(AS_DECLARED, null);
  }

  static TypeElement asType(Element element) {
    TypeElement result = element.accept(AS_TYPE_ELEMENT, null);
    if (result == null) {
      throw new IllegalArgumentException("Not a TypeElement: " + element);
    }
    return result;
  }

  public static boolean equalsType(TypeMirror typeMirror, String qualified) {
    return qualified.equals(typeMirror.accept(QUALIFIED_NAME, null));
  }

  public static DeclaredType asParameterized(TypeMirror mirror) {
    if (mirror.getKind() != TypeKind.DECLARED) {
      return null;
    }
    DeclaredType declared = mirror.accept(AS_DECLARED, null);
    if (declared.getTypeArguments().isEmpty()) {
      return null;
    }
    return declared;
  }

  static String snakeCase(String input) {
    StringBuilder sb = new StringBuilder();
    int prevLower = 0;
    int prevUpper = 0;
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (Character.isUpperCase(c)) {
        if (prevLower >= 2) {
          sb.append('_');
        }
        prevLower = 0;
        prevUpper++;
      } else {
        if (prevUpper >= 2) {
          sb.append('_');
        }
        prevUpper = 0;
        prevLower++;
      }
      sb.append(Character.toLowerCase(c));
    }
    return sb.toString();
  }

  static String methodToString(ExecutableElement method) {
    StringJoiner joiner = new StringJoiner(", ", "(", ")");
    for (VariableElement variableElement : method.getParameters()) {
      joiner.add(variableElement.asType().toString());
    }
    return method.getSimpleName() + joiner.toString();
  }
}
