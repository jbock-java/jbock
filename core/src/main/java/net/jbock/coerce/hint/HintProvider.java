package net.jbock.coerce.hint;

import net.jbock.coerce.OptionalInfo;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.jbock.coerce.OptionalInfo.findKind;


public class HintProvider {

  private static final List<Hint> HINTS = Arrays.asList(
      new RawCombinatorHint(),
      new CollectionHint(),
      new ArrayHint(),
      new PrimitiveHint(),
      new DateHint(),
      new OptionalHint());

  private static HintProvider instance;

  public static HintProvider instance() {
    if (instance == null) {
      instance = new HintProvider();
    }
    return instance;
  }

  public Optional<String> findHint(TypeMirror type, boolean repeatable, boolean optional) {
    if (type.getKind() != TypeKind.DECLARED) {
      return findHintSimple(type, repeatable);
    }
    if (optional) {
      OptionalInfo optionalInfo = findKind(type, true);
      if (optionalInfo.optional) {
        return findHintSimple(optionalInfo.baseType, repeatable);
      }
    }
    return findHintSimple(type, repeatable);
  }

  private Optional<String> findHintSimple(TypeMirror type, boolean repeatable) {
    for (Hint warning : HINTS) {
      String message = warning.message(type, repeatable);
      if (message != null) {
        return Optional.of(message);
      }
    }
    return Optional.empty();
  }
}
