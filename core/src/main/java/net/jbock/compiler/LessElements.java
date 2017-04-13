package net.jbock.compiler;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleElementVisitor6;

import static javax.lang.model.element.ElementKind.PACKAGE;

public final class LessElements {

  private static final ElementVisitor<TypeElement, Void> TYPE_ELEMENT_VISITOR =
      new SimpleElementVisitor6<TypeElement, Void>() {
        @Override
        protected TypeElement defaultAction(Element e, Void p) {
          throw new IllegalArgumentException();
        }

        @Override
        public TypeElement visitType(TypeElement e, Void p) {
          return e;
        }
      };

  private static final ElementVisitor<ExecutableElement, Void> EXECUTABLE_ELEMENT_VISITOR =
      new SimpleElementVisitor6<ExecutableElement, Void>() {
        @Override
        protected ExecutableElement defaultAction(Element e, Void p) {
          throw new IllegalArgumentException();
        }

        @Override
        public ExecutableElement visitExecutable(ExecutableElement e, Void p) {
          return e;
        }
      };

  private static PackageElement getPackage(Element element) {
    while (element.getKind() != PACKAGE) {
      element = element.getEnclosingElement();
    }
    return (PackageElement) element;
  }

  public static ExecutableElement asExecutable(Element element) {
    return element.accept(EXECUTABLE_ELEMENT_VISITOR, null);
  }

  static TypeElement asType(Element element) {
    return element.accept(TYPE_ELEMENT_VISITOR, null);
  }

  private LessElements() {
    throw new UnsupportedOperationException("no instances");
  }
}
