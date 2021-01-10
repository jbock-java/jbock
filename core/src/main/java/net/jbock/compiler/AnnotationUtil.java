package net.jbock.compiler;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;
import java.util.Map;
import java.util.Optional;

class AnnotationUtil {

  private final TypeTool tool;
  private final ExecutableElement sourceMethod;

  private static final AnnotationValueVisitor<TypeMirror, Void> GET_TYPE = new SimpleAnnotationValueVisitor8<TypeMirror, Void>() {

    @Override
    public TypeMirror visitType(TypeMirror mirror, Void _null) {
      return mirror;
    }
  };

  private static final TypeVisitor<Boolean, TypeTool> IS_VOID = new SimpleTypeVisitor8<Boolean, TypeTool>() {
    @Override
    protected Boolean defaultAction(TypeMirror e, TypeTool tool) {
      return false;
    }

    @Override
    public Boolean visitDeclared(DeclaredType type, TypeTool tool) {
      TypeElement element = type.asElement().accept(TypeTool.AS_TYPE_ELEMENT, null);
      return element != null && "java.lang.Void".equals(element.getQualifiedName().toString());
    }
  };

  AnnotationUtil(TypeTool tool, ExecutableElement sourceMethod) {
    this.tool = tool;
    this.sourceMethod = sourceMethod;
  }

  Optional<TypeElement> get(Class<?> annotationClass, String attributeName) {
    AnnotationMirror annotation = getAnnotationMirror(tool, sourceMethod, annotationClass);
    if (annotation == null) {
      // if the source method doesn't have this annotation
      return Optional.empty();
    }
    AnnotationValue annotationValue = getAnnotationValue(annotation, attributeName);
    if (annotationValue == null) {
      // if the default value is not overridden
      return Optional.empty();
    }
    TypeMirror typeMirror = annotationValue.accept(GET_TYPE, null);
    if (typeMirror == null) {
      throw ValidationException.create(sourceMethod, String.format("Invalid value of attribute '%s'.", attributeName));
    }
    if (typeMirror.accept(IS_VOID, tool)) {
      // if the default value is not overridden
      return Optional.empty();
    }
    return Optional.of(tool.asTypeElement(typeMirror));
  }

  private static AnnotationMirror getAnnotationMirror(TypeTool tool, ExecutableElement sourceMethod, Class<?> annotationClass) {
    return sourceMethod.getAnnotationMirrors().stream()
        .filter(m -> tool.isSameType(m.getAnnotationType(), annotationClass.getCanonicalName()))
        .findAny().orElse(null);
  }

  private static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
    return annotationMirror.getElementValues().entrySet().stream()
        .filter(entry -> entry.getKey().getSimpleName().toString().equals(key))
        .map(Map.Entry::getValue).findAny().orElse(null);
  }
}
