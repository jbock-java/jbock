package net.jbock.qualifier;

public class DescriptionKeys {

  private final boolean anyDescriptionKeys;

  private DescriptionKeys(boolean anyDescriptionKeys) {
    this.anyDescriptionKeys = anyDescriptionKeys;
  }

  public static DescriptionKeys create(AllParameters allParameters, SourceElement sourceElement) {
    return new DescriptionKeys(allParameters.anyDescriptionKeys()
        || sourceElement.descriptionKey().isPresent());
  }

  public boolean anyDescriptionKeys() {
    return anyDescriptionKeys;
  }
}
