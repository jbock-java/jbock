package net.jbock.compiler;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import java.util.Map;

final class AnnotationUtil {

  private static final class AnnotationUtilContext {

    final ExecutableElement sourceMethod;
    final String attributeName;

    AnnotationUtilContext(ExecutableElement sourceMethod, String attributeName) {
      this.sourceMethod = sourceMethod;
      this.attributeName = attributeName;
    }
  }

  private static final AnnotationValueVisitor<TypeMirror, AnnotationUtilContext> GET_TYPE = new SimpleAnnotationValueVisitor8<TypeMirror, AnnotationUtilContext>() {

    @Override
    protected TypeMirror defaultAction(Object o, AnnotationUtilContext utilContext) {
      throw ValidationException.create(utilContext.sourceMethod,
          String.format("Invalid value of '%s'.", utilContext.attributeName));
    }

    @Override
    public TypeMirror visitType(TypeMirror mirror, AnnotationUtilContext utilContext) {
      return mirror;
    }
  };

  private static TypeElement get(
      ExecutableElement sourceMethod,
      Class<?> annotationClass,
      String attributeName) {
    AnnotationMirror annotation = getAnnotationMirror(sourceMethod, annotationClass);
    if (annotation == null) {
      // if the source method doesn't have this annotation
      return null;
    }
    AnnotationValue annotationValue = getAnnotationValue(annotation, attributeName);
    if (annotationValue == null) {
      // if the default value is not overridden
      return null;
    }
    TypeMirror typeMirror = annotationValue.accept(GET_TYPE, new AnnotationUtilContext(sourceMethod, attributeName));
    if (typeMirror.getKind() != TypeKind.DECLARED) {
      throw ValidationException.create(sourceMethod,
          String.format("Invalid value of '%s'.", attributeName));
    }
    return TypeTool.get().asTypeElement(typeMirror);
  }


  static TypeElement getMapperClass(ExecutableElement sourceMethod, Class<?> annotationClass) {
    return get(sourceMethod, annotationClass, "mappedBy");
  }

  static TypeElement getCollectorClass(ExecutableElement sourceMethod, Class<?> annotationClass) {
    return get(sourceMethod, annotationClass, "collectedBy");
  }

  private static AnnotationMirror getAnnotationMirror(ExecutableElement sourceMethod, Class<?> annotationClass) {
    for (AnnotationMirror m : sourceMethod.getAnnotationMirrors()) {
      if (TypeTool.get().eql(m.getAnnotationType(), annotationClass)) {
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
