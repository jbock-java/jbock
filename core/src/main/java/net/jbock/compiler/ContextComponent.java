package net.jbock.compiler;

import dagger.Component;
import net.jbock.compiler.view.GeneratedClass;

@Component(modules = ContextModule.class)
public interface ContextComponent {

  GeneratedClass generatedClass();

  @Component.Factory
  interface Builder {

    ContextComponent create(
        ContextModule module);
  }
}
