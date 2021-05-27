package net.jbock.validate;

import dagger.Module;
import dagger.Provides;
import net.jbock.common.TypeTool;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@Module
public interface CommandModule {

  @Provides
  static Types types(TypeTool tool) {
    return tool.types();
  }

  @Provides
  static Elements elements(TypeTool tool) {
    return tool.elements();
  }
}
