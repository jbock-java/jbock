package net.jbock.compiler;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Map;

final class MapperClassUtil {

  private static TypeElement get(
      ExecutableElement sourceMethod,
      Class<?> annotationClass,
      String attributeName) {
    AnnotationMirror mirror = getAnnotationMirror(sourceMethod, annotationClass);
    if (mirror == null) {
      // the source method doesn't have this annotation
      return null;
    }
    AnnotationValue annotationValue = getAnnotationValue(mirror, attributeName);
    if (annotationValue == null) {
      // if the default value of mappedBy is not overridden
      return null;
    }
    Object annotationValueValue = annotationValue.getValue();
    if (!(annotationValueValue instanceof TypeMirror)) {
      // can't happen because mapperClass() returns Class
      return null;
    }
    TypeMirror typeMirror = (TypeMirror) annotationValueValue;
    if (typeMirror.getKind() != TypeKind.DECLARED) {
      return null;
    }
    DeclaredType declaredType = typeMirror.accept(Util.AS_DECLARED, null);
    return declaredType.asElement().accept(Util.AS_TYPE_ELEMENT, null);
  }


  static TypeElement getMapperClass(ExecutableElement sourceMethod, Class<?> annotationClass) {
    return get(sourceMethod, annotationClass, "mappedBy");
  }

  static TypeElement getCollectorClass(ExecutableElement sourceMethod, Class<?> annotationClass) {
    return get(sourceMethod, annotationClass, "collectedBy");
  }

  private static AnnotationMirror getAnnotationMirror(ExecutableElement sourceMethod, Class<?> clazz) {
    String clazzName = clazz.getName();
    for (AnnotationMirror m : sourceMethod.getAnnotationMirrors()) {
      if (m.getAnnotationType().toString().equals(clazzName)) {
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
