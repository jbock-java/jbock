package net.jbock.coerce.hint;

import net.jbock.coerce.BasicInfo;

import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class HintProvider {

  private static final List<Hint> HINTS = Arrays.asList(
      new RawCombinatorHint(),
      new CollectionHint(),
      new ArrayHint(),
      new DateHint(),
      new OptionalHint());

  public static Optional<String> findHint(BasicInfo basicInfo) {
    if (basicInfo.isOptional()) {
      return findHintSimple(basicInfo.optionalInfo().get(), basicInfo.isRepeatable());
    }
    return findHintSimple(basicInfo.originalReturnType(), basicInfo.isRepeatable());
  }

  private static Optional<String> findHintSimple(TypeMirror type, boolean repeatable) {
    for (Hint warning : HINTS) {
      String message = warning.message(type, repeatable);
      if (message != null) {
        return Optional.of(message);
      }
    }
    return Optional.empty();
  }
}
