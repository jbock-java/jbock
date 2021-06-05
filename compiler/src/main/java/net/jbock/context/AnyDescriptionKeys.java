package net.jbock.context;

import net.jbock.processor.SourceElement;

import javax.inject.Inject;

@ContextScope
public class AnyDescriptionKeys {

  private final AllItems allItems;
  private final SourceElement sourceElement;

  @Inject
  AnyDescriptionKeys(AllItems allItems, SourceElement sourceElement) {
    this.allItems = allItems;
    this.sourceElement = sourceElement;
  }

  public boolean anyDescriptionKeysAtAll() {
    return allItems.anyDescriptionKeys()
        || sourceElement.descriptionKey().isPresent();
  }
}
