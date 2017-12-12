package net.jbock.compiler;

import static java.util.Locale.US;

import java.util.StringJoiner;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;

final class Util {

  private static final SimpleTypeVisitor8<DeclaredType, Void> AS_DECLARED =
      new SimpleTypeVisitor8<DeclaredType, Void>() {
        @Override
        public DeclaredType visitDeclared(DeclaredType declaredType, Void _null) {
          return declaredType;
        }
      };

  private static final SimpleElementVisitor8<TypeElement, Void> AS_TYPE_ELEMENT =
      new SimpleElementVisitor8<TypeElement, Void>() {
        @Override
        public TypeElement visitType(TypeElement typeElement, Void _null) {
          return typeElement;
        }
      };

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

  static boolean equalsType(TypeMirror typeMirror, String qualified) {
    DeclaredType declared = typeMirror.accept(AS_DECLARED, null);
    if (declared == null) {
      return false;
    }
    TypeElement typeElement = declared.asElement().accept(AS_TYPE_ELEMENT, null);
    if (typeElement == null) {
      return false;
    }
    return typeElement.getQualifiedName().toString().equals(qualified);
  }

  static String snakeCase(String input) {
    if (Character.isUpperCase(input.charAt(0))) {
      return input.toUpperCase(US);
    }
    if (input.indexOf('_') >= 0) {
      return input.toUpperCase(US);
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (Character.isUpperCase(c)) {
        if (i > 0) {
          sb.append('_');
        }
        sb.append(c);
      } else {
        sb.append(Character.toUpperCase(c));
      }
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
