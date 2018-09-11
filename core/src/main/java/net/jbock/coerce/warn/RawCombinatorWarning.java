package net.jbock.coerce.warn;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import static net.jbock.compiler.Util.AS_TYPE_ELEMENT;
import static net.jbock.compiler.Util.asDeclared;

public class RawCombinatorWarning extends Warning {

  @Override
  public String message(TypeMirror type) {
    if (type.getKind().isPrimitive()) {
      return null;
    }
    DeclaredType declared = asDeclared(type);
    if (declared == null) {
      return null;
    }
    TypeElement typeElement = declared.asElement().accept(AS_TYPE_ELEMENT, null);
    if (typeElement == null) {
      return null;
    }
    if (!declared.getTypeArguments().isEmpty()) {
      return null;
    }
    String qname = typeElement.getQualifiedName().toString();
    if ("java.util.List".equals(qname)) {
      return "Raw lists are not supported. Use List<X>.";
    }
    if ("java.util.Optional".equals(qname)) {
      return "Raw optionals are not supported. Use Optional<X>.";
    }
    return null;
  }
}
