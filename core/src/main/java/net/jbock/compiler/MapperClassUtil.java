package net.jbock.compiler;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import java.util.Map;

final class MapperClassUtil {

  private static final class MapperClassUtilContext {

    final ExecutableElement sourceMethod;
    final String attributeName;

    MapperClassUtilContext(ExecutableElement sourceMethod, String attributeName) {
      this.sourceMethod = sourceMethod;
      this.attributeName = attributeName;
    }
  }

  private static final AnnotationValueVisitor<TypeMirror, MapperClassUtilContext> GET_TYPE = new SimpleAnnotationValueVisitor8<TypeMirror, MapperClassUtilContext>() {

    @Override
    protected TypeMirror defaultAction(Object o, MapperClassUtilContext utilContext) {
      throw ValidationException.create(utilContext.sourceMethod,
          String.format("Invalid value of '%s'.", utilContext.attributeName));
    }

    @Override
    public TypeMirror visitType(TypeMirror mirror, MapperClassUtilContext utilContext) {
      return mirror;
    }
  };

  private static TypeElement get(
      ExecutableElement sourceMethod,
      Class<?> annotationClass,
      String attributeName) {
    AnnotationMirror mirror = getAnnotationMirror(sourceMethod, annotationClass);
    if (mirror == null) {
      // if the source method doesn't have this annotation
      return null;
    }
    AnnotationValue annotationValue = getAnnotationValue(mirror, attributeName);
    if (annotationValue == null) {
      // if the default value is not overridden
      return null;
    }
    TypeMirror typeMirror = annotationValue.accept(GET_TYPE, new MapperClassUtilContext(sourceMethod, attributeName));
    if (typeMirror.getKind() != TypeKind.DECLARED) {
      throw ValidationException.create(sourceMethod,
          String.format("Invalid value of '%s'.", attributeName));
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
