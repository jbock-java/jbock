package net.jbock.compiler;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;

final class Util {

  static final SimpleTypeVisitor8<DeclaredType, Void> AS_DECLARED =
      new SimpleTypeVisitor8<DeclaredType, Void>() {
        @Override
        public DeclaredType visitDeclared(DeclaredType declaredType, Void _null) {
          return declaredType;
        }
      };

  static final SimpleElementVisitor8<TypeElement, Void> AS_TYPE_ELEMENT =
      new SimpleElementVisitor8<TypeElement, Void>() {
        @Override
        public TypeElement visitType(TypeElement typeElement, Void _null) {
          return typeElement;
        }
      };

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
}
