package net.jbock.coerce.warn;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static net.jbock.compiler.Util.AS_DECLARED;
import static net.jbock.compiler.Util.AS_TYPE_ELEMENT;

public class RawCombinatorWarning extends Warning {

  @Override
  public String message(TypeMirror mirror) {
    if (mirror.getKind() != TypeKind.DECLARED) {
      return null;
    }
    DeclaredType declared = mirror.accept(AS_DECLARED, null);
    if (!declared.getTypeArguments().isEmpty()) {
      return null;
    }
    TypeElement typeElement = declared.asElement().accept(AS_TYPE_ELEMENT, null);
    if (typeElement.getTypeParameters().isEmpty()) {
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
