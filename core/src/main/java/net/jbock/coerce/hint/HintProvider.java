package net.jbock.coerce.hint;

import net.jbock.coerce.BasicInfo;

import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class HintProvider {

  private final BasicInfo basicInfo;

  private static final List<Hint> HINTS = Arrays.asList(
      new RawCombinatorHint(),
      new DateHint());

  public HintProvider(BasicInfo basicInfo) {
    this.basicInfo = basicInfo;
  }

  public Optional<String> findHint() {
    if (basicInfo.isOptional()) {
      return findHintSimple(
          basicInfo.tool().asTypeElement(basicInfo.optionalInfo().get()),
          basicInfo.isRepeatable());
    }
    return findHintSimple(
        basicInfo.tool().asTypeElement(basicInfo.returnType()),
        basicInfo.isRepeatable());
  }

  private Optional<String> findHintSimple(TypeElement type, boolean repeatable) {
    for (Hint warning : HINTS) {
      String message = warning.message(type, repeatable);
      if (message != null) {
        return Optional.of(message);
      }
    }
    return Optional.empty();
  }
}
