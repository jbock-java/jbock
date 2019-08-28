package net.jbock.compiler;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;
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

  private static final TypeVisitor<Boolean, TypeTool> IS_JAVA_LANG_OBJECT = new SimpleTypeVisitor8<Boolean, TypeTool>() {
    @Override
    protected Boolean defaultAction(TypeMirror e, TypeTool tool) {
      return false;
    }

    @Override
    public Boolean visitDeclared(DeclaredType type, TypeTool tool) {
      TypeElement element = tool.asTypeElement(type.asElement());
      return "java.lang.Object".equals(element.getQualifiedName().toString());
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
    TypeTool tool = TypeTool.get();
    if (typeMirror.accept(IS_JAVA_LANG_OBJECT, tool)) {
      // if the default value is not overridden
      return null;
    }
    if (typeMirror.getKind() != TypeKind.DECLARED) {
      throw ValidationException.create(sourceMethod,
          String.format("Invalid value of '%s'.", attributeName));
    }
    return tool.asTypeElement(typeMirror);
  }


  static TypeElement getMapperClass(ExecutableElement sourceMethod, Class<?> annotationClass) {
    return get(sourceMethod, annotationClass, "mappedBy");
  }

  static TypeElement getCollectorClass(ExecutableElement sourceMethod, Class<?> annotationClass) {
    return get(sourceMethod, annotationClass, "collectedBy");
  }

  private static AnnotationMirror getAnnotationMirror(ExecutableElement sourceMethod, Class<?> annotationClass) {
    TypeTool tool = TypeTool.get();
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
