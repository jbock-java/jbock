package net.jbock.qualifier;

import net.jbock.scope.ContextScope;

import javax.inject.Inject;

@ContextScope
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
