package net.jbock.validate;

import dagger.Module;
import dagger.Provides;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;

import javax.lang.model.util.Types;

@Module
public class ValidateModule {

  private final Types types;
  private final SafeElements elements;

  public ValidateModule(Types types, SafeElements elements) {
    this.types = types;
    this.elements = elements;
  }

  @Provides
  Types types(TypeTool tool) {
    return types;
  }

  @Provides
  SafeElements elements() {
    return elements;
  }
}
