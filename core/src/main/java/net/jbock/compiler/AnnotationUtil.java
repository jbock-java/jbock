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
  private final Class<?> annotationClass;
  private final String attributeName;

  private static final AnnotationValueVisitor<TypeMirror, Void> GET_TYPE = new SimpleAnnotationValueVisitor8<TypeMirror, Void>() {

    @Override
    public TypeMirror visitType(TypeMirror mirror, Void _null) {
      return mirror;
    }
  };

  private static final TypeVisitor<Boolean, TypeTool> IS_JAVA_LANG_OBJECT = new SimpleTypeVisitor8<Boolean, TypeTool>() {
    @Override
    protected Boolean defaultAction(TypeMirror e, TypeTool tool) {
      return false;
    }

    @Override
    public Boolean visitDeclared(DeclaredType type, TypeTool tool) {
      TypeElement element = type.asElement().accept(TypeTool.AS_TYPE_ELEMENT, null);
      if (element == null) {
        return false;
      }
      return "java.lang.Object".equals(element.getQualifiedName().toString());
    }
  };

  private AnnotationUtil(TypeTool tool, ExecutableElement sourceMethod, Class<?> annotationClass, String attributeName) {
    this.tool = tool;
    this.sourceMethod = sourceMethod;
    this.annotationClass = annotationClass;
    this.attributeName = attributeName;
  }

  private Optional<TypeElement> get() {
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
    if (typeMirror.accept(IS_JAVA_LANG_OBJECT, tool)) {
      // if the default value is not overridden
      return Optional.empty();
    }
    return Optional.of(tool.asTypeElement(typeMirror));
  }


  static Optional<TypeElement> getMapperClass(TypeTool tool, ExecutableElement sourceMethod, Class<?> annotationClass) {
    return new AnnotationUtil(tool, sourceMethod, annotationClass, "mappedBy").get();
  }

  static Optional<TypeElement> getCollectorClass(TypeTool tool, ExecutableElement sourceMethod, Class<?> annotationClass) {
    return new AnnotationUtil(tool, sourceMethod, annotationClass, "collectedBy").get();
  }

  private static AnnotationMirror getAnnotationMirror(TypeTool tool, ExecutableElement sourceMethod, Class<?> annotationClass) {
    for (AnnotationMirror m : sourceMethod.getAnnotationMirrors()) {
      if (tool.isSameType(m.getAnnotationType(), annotationClass)) {
        return m;
      }
    }
    return null;
  }

  private static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
    Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationMirror.getElementValues();
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
      String simpleName = entry.getKey().getSimpleName().toString();
      if (simpleName.equals(key)) {
        return entry.getValue();
      }
    }
    return null;
  }
}
