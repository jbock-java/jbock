package net.jbock.compiler;

import dagger.Component;
import net.jbock.compiler.view.GeneratedClass;

@Component(modules = ContextModule.class)
interface ContextComponent {

  GeneratedClass generatedClass();

  @Component.Factory
  interface Builder {

    ContextComponent create(
        ContextModule module);
  }
}
