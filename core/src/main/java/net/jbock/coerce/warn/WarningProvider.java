package net.jbock.coerce.warn;

import net.jbock.coerce.TriggerKind;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;

import static net.jbock.coerce.CoercionKind.findKind;

public class WarningProvider {

  private static final List<Warning> WARNINGS = Arrays.asList(
      new RawCombinatorWarning(),
      new CollectionWarning(),
      new ArrayWarning(),
      new PrimitiveWarning(),
      new DateWarning(),
      new OptionalWarning());

  private static WarningProvider instance;

  public static WarningProvider instance() {
    if (instance == null) {
      instance = new WarningProvider();
    }
    return instance;
  }

  public String findWarning(TypeMirror type, boolean repeatable, boolean optional) {
    if (type.getKind() != TypeKind.DECLARED) {
      return findWarningSimple(type, repeatable);
    }
    if (optional) {
      TriggerKind tk = findKind(type);
      if (tk.kind.isCombination()) {
        return findWarningSimple(tk.trigger, repeatable);
      }
    }
    return findWarningSimple(type, repeatable);
  }

  private String findWarningSimple(TypeMirror type, boolean repeatable) {
    for (Warning warning : WARNINGS) {
      String message = warning.message(type, repeatable);
      if (message != null) {
        return message;
      }
    }
    return null;
  }
}
