package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;
import java.util.Optional;
import java.util.StringJoiner;

import static net.jbock.com.squareup.javapoet.WildcardTypeName.subtypeOf;

final class Util {

  private static final SimpleTypeVisitor8<DeclaredType, Void> AS_DECLARED =
      new SimpleTypeVisitor8<DeclaredType, Void>() {
        @Override
        public DeclaredType visitDeclared(DeclaredType declaredType, Void _null) {
          return declaredType;
        }
      };

  private static final SimpleTypeVisitor8<ArrayType, Void> AS_ARRAY =
      new SimpleTypeVisitor8<ArrayType, Void>() {
        @Override
        public ArrayType visitArray(ArrayType t, Void aVoid) {
          return t;
        }
      };

  private static final SimpleElementVisitor8<TypeElement, Void> AS_TYPE_ELEMENT =
      new SimpleElementVisitor8<TypeElement, Void>() {
        @Override
        public TypeElement visitType(TypeElement typeElement, Void _null) {
          return typeElement;
        }
      };

  static ParameterizedTypeName optionalOf(TypeName typeName) {
    return ParameterizedTypeName.get(ClassName.get(Optional.class), typeName);
  }

  static ParameterizedTypeName optionalOfSubtype(TypeName typeName) {
    return ParameterizedTypeName.get(ClassName.get(Optional.class), subtypeOf(typeName));
  }

  static DeclaredType asDeclared(TypeMirror mirror) {
    return mirror.accept(AS_DECLARED, null);
  }

  static ArrayType asArray(TypeMirror mirror) {
    return mirror.accept(AS_ARRAY, null);
  }

  static TypeElement asType(Element element) {
    TypeElement result = element.accept(AS_TYPE_ELEMENT, null);
    if (result == null) {
      throw new IllegalArgumentException("Not a TypeElement: " + element);
    }
    return result;
  }

  static boolean equalsType(TypeMirror typeMirror, String qualified) {
    DeclaredType declared = typeMirror.accept(AS_DECLARED, null);
    if (declared == null) {
      return false;
    }
    TypeElement typeElement = declared.asElement().accept(AS_TYPE_ELEMENT, null);
    return typeElement != null &&
        typeElement.getQualifiedName().toString().equals(qualified);
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
