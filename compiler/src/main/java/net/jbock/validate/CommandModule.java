package net.jbock.validate;

import dagger.Module;
import dagger.Provides;
import net.jbock.common.TypeTool;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@Module
public class CommandModule {

  private final Types types;
  private final Elements elements;

  public CommandModule(Types types, Elements elements) {
    this.types = types;
    this.elements = elements;
  }

  @Provides
  Types types(TypeTool tool) {
    return types;
  }

  @Provides
  Elements elements() {
    return elements;
  }
}
