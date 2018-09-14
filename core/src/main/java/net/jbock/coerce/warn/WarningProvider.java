package net.jbock.coerce.warn;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;

import static net.jbock.coerce.CoercionProvider.isCombinator;
import static net.jbock.compiler.Util.AS_DECLARED;
import static net.jbock.compiler.Util.AS_TYPE_ELEMENT;

public class WarningProvider {

  private static final List<Warning> WARNINGS = Arrays.asList(
      new RawCombinatorWarning(),
      new CollectionWarning(),
      new ArrayWarning(),
      new PrimitiveWarning(),
      new DateWarning());

  private static WarningProvider instance;

  public static WarningProvider instance() {
    if (instance == null) {
      instance = new WarningProvider();
    }
    return instance;
  }

  public String findWarning(TypeMirror type) {
    if (type.getKind() != TypeKind.DECLARED) {
      return findWarningSimple(type);
    }
    DeclaredType declared = type.accept(AS_DECLARED, null);
    if (isCombinator(type) && !declared.getTypeArguments().isEmpty()) {
      TypeElement typeElement = declared.asElement().accept(AS_TYPE_ELEMENT, null);
      return findWarningSimple(typeElement.getTypeParameters().get(0).asType());
    }
    return findWarningSimple(type);
  }

  private String findWarningSimple(TypeMirror type) {
    for (Warning warning : WARNINGS) {
      String message = warning.message(type);
      if (message != null) {
        return message;
      }
    }
    return null;
  }
}
