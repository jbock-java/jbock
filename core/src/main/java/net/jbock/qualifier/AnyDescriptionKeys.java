package net.jbock.qualifier;

import dagger.Reusable;

import javax.inject.Inject;

@Reusable
public class AnyDescriptionKeys {

  private final AllParameters allParameters;
  private final SourceElement sourceElement;

  @Inject
  AnyDescriptionKeys(AllParameters allParameters, SourceElement sourceElement) {
    this.allParameters = allParameters;
    this.sourceElement = sourceElement;
  }

  public boolean anyDescriptionKeysAtAll() {
    return allParameters.anyDescriptionKeys()
        || sourceElement.descriptionKey().isPresent();
  }
}
